package com.bf.resource;

import com.bf.Model.Task;
import com.bf.Repository.AppUserRepository;
import com.bf.Service.MsProjectImportService;
import com.bf.Service.TaskService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/api/import")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)
public class ImportResource {

    @Inject
    MsProjectImportService importer;

    @Inject
    TaskService taskService;

    @Inject
    AppUserRepository appUserRepository;

    @POST
    @Path("/projects/{id}/msproject")
    @RolesAllowed({ "ADMIN", "PROJECT_LEAD" })
    public Response importMsProject(@PathParam("id") UUID projectId,
                                    @RestForm("file") FileUpload file) {
        if (file == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Missing file form field 'file' (xml)"))
                    .build();
        }

        try (InputStream is = Files.newInputStream(file.uploadedFile())) {
            List<MsProjectImportService.ImportedTask> imported = importer.parse(is);

            Map<String, UUID> uidToTaskId = new HashMap<>();
            List<Map<String, Object>> results = new ArrayList<>();

            for (MsProjectImportService.ImportedTask it : imported) {
                Task created = taskService.createFromImport(projectId, it);
                uidToTaskId.put(it.uid, created.id);
                results.add(Map.of("uid", it.uid, "taskId", created.id));

                if (it.resourceAssignments != null && !it.resourceAssignments.isEmpty()) {
                    for (MsProjectImportService.ResourceAssign ra : it.resourceAssignments) {
                        try {
                            if (ra.resourceName == null) {
                                results.add(Map.of("uid", it.uid, "unmappedResource", "(unknown name)"));
                                continue;
                            }

                            var maybeUser = appUserRepository.findByUsername(ra.resourceName)
                                    .or(() -> appUserRepository.findByEmail(ra.resourceName))
                                    .or(() -> appUserRepository.findByFullName(ra.resourceName));

                            if (maybeUser.isPresent()) {
                                var u = maybeUser.get();
                                java.math.BigDecimal hours = java.math.BigDecimal.valueOf(ra.minutes / 60.0);
                                taskService.assignUserToTask(created.id, u.id, hours);
                                results.add(Map.of("uid", it.uid, "assignedTo", u.id, "hours", hours));
                            } else {
                                results.add(Map.of("uid", it.uid, "unmappedResource", ra.resourceName));
                            }
                        } catch (Exception ex) {
                            results.add(Map.of("uid", it.uid, "assignError", ex.getMessage()));
                        }
                    }
                }
            }

            for (MsProjectImportService.ImportedTask it : imported) {
                for (String p : it.predecessors) {
                    UUID pred = uidToTaskId.get(p);
                    UUID succ = uidToTaskId.get(it.uid);

                    if (pred != null && succ != null && !pred.equals(succ)) {
                        try {
                            taskService.createDependency(pred, succ, 0);
                        } catch (Exception ex) {
                            results.add(Map.of("uid", it.uid, "depError", ex.getMessage()));
                        }
                    }
                }
            }

            return Response.ok(Map.of("imported", results)).build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("message", ex.getMessage()))
                    .build();
        }
    }
}