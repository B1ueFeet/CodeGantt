package com.bf.resource;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import com.bf.Repository.TaskAssigneeRepository;
import com.bf.Model.TaskAssignee;

import java.util.*;
import java.util.stream.Collectors;
import java.util.UUID;

@Path("/api/leaders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"USER","ADMIN"})
public class LeaderResource {

    private final TaskAssigneeRepository assignees;

    public LeaderResource(TaskAssigneeRepository assignees) {
        this.assignees = assignees;
    }

    @GET
    @Path("/{userId}/summary")
    public Map<String,Object> summary(@PathParam("userId") UUID userId) {
        var list = assignees.list("user.id", userId);
        var byProject = new HashMap<UUID, List<TaskAssignee>>();
        for (var a : list) {
            var pid = a.task.project.id;
            byProject.computeIfAbsent(pid, k -> new ArrayList<>()).add(a);
        }

        var projects = new ArrayList<Map<String,Object>>();
        for (var entry : byProject.entrySet()) {
            var pid = entry.getKey();
            var items = entry.getValue();
            double total = items.stream().mapToDouble(x -> x.assignedHours.doubleValue()).sum();
            var tasks = items.stream().map(x -> Map.of("taskId", x.task.id, "name", x.task.name, "assignedHours", x.assignedHours)).collect(Collectors.toList());
            projects.add(Map.of("projectId", pid, "totalAssignedHours", total, "tasks", tasks));
        }

        return Map.of("projects", projects);
    }
}
