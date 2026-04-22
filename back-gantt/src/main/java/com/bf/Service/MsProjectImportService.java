package com.bf.Service;

import jakarta.enterprise.context.ApplicationScoped;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@ApplicationScoped
public class MsProjectImportService { // Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>

    // Parse an MS Project XML input stream and return list of task DTOs (map)
    public static class ImportedTask {
        public String uid;
        public String name;
        public OffsetDateTime start;
        public long durationMinutes;
        public List<String> predecessors = new ArrayList<>();
        public List<ResourceAssign> resourceAssignments = new ArrayList<>();
    }

    public static class ResourceAssign {
        public String resourceName; // may be null if not matched
        public long minutes;
        public ResourceAssign(String resourceName, long minutes) { this.resourceName = resourceName; this.minutes = minutes; }
    }

    public List<ImportedTask> parse(InputStream xmlStream) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(xmlStream);

        // find MinutesPerDay
        NodeList mins = doc.getElementsByTagName("MinutesPerDay");
        int minutesPerDay = 480;
        if (mins.getLength() > 0) {
            try { minutesPerDay = Integer.parseInt(mins.item(0).getTextContent().trim()); } catch (Exception ignored) {}
        }

        // build resource map: Resource UID -> Name
        Map<String,String> resourceUidToName = new HashMap<>();
        NodeList resources = doc.getElementsByTagName("Resource");
        for (int i=0;i<resources.getLength();i++){
            Element r = (Element) resources.item(i);
            String ru = getChildText(r,"UID");
            String rn = getChildText(r,"Name");
            if (ru != null && rn != null) resourceUidToName.put(ru.trim(), rn.trim());
        }

        // build assignment map: TaskUID -> list of ResourceAssign
        Map<String, List<ResourceAssign>> assignmentsByTask = new HashMap<>();
        NodeList assigns = doc.getElementsByTagName("Assignment");
        for (int i=0;i<assigns.getLength();i++){
            Element a = (Element) assigns.item(i);
            String taskUid = getChildText(a,"TaskUID");
            String resourceUid = getChildText(a,"ResourceUID");
            String work = getChildText(a,"Work");
            long minutes = parseDurationToMinutes(work, minutesPerDay);
            String rname = resourceUidToName.getOrDefault(resourceUid, null);
            if (taskUid == null) continue;
            assignmentsByTask.computeIfAbsent(taskUid.trim(), k -> new ArrayList<>())
                    .add(new ResourceAssign(rname, minutes));
        }

        List<ImportedTask> out = new ArrayList<>();
        NodeList tasks = doc.getElementsByTagName("Task");
        for (int i=0;i<tasks.getLength();i++){
            Element t = (Element) tasks.item(i);
            ImportedTask it = new ImportedTask();
            String uid = getChildText(t,"UID");
            if (uid == null) continue; // skip empty
            it.uid = uid;
            it.name = getChildText(t,"Name");
            String start = getChildText(t,"Start");
            if (start != null) {
                try { it.start = OffsetDateTime.parse(start, DateTimeFormatter.ISO_OFFSET_DATE_TIME); }
                catch(Exception ex) { it.start = OffsetDateTime.now(); }
            } else {
                it.start = OffsetDateTime.now();
            }
            String duration = getChildText(t,"Duration");
            it.durationMinutes = parseDurationToMinutes(duration, minutesPerDay);

            // predecessors
            NodeList preds = t.getElementsByTagName("PredecessorUID");
            for (int p=0;p<preds.getLength();p++){
                it.predecessors.add(preds.item(p).getTextContent().trim());
            }

            // attach resource assignments if any
            var assignsForTask = assignmentsByTask.get(uid.trim());
            if (assignsForTask != null) {
                it.resourceAssignments.addAll(assignsForTask);
            }

            out.add(it);
        }

        return out;
    }

    private long parseDurationToMinutes(String durationIso, int minutesPerDay){
        if (durationIso == null) return 0;
        // Example: PT16H0M0S or PT52H0M0S or PT0H0M0S
        try{
            String s = durationIso.toUpperCase();
            if (!s.startsWith("PT")) return 0;
            s = s.substring(2);
            int hIndex = s.indexOf('H');
            long hours = 0;
            long minutes = 0;
            if (hIndex > -1) {
                hours = Long.parseLong(s.substring(0,hIndex));
                s = s.substring(hIndex+1);
            }
            int mIndex = s.indexOf('M');
            if (mIndex > -1) {
                minutes = Long.parseLong(s.substring(0,mIndex));
            }
            return hours*60 + minutes;
        }catch(Exception ex){
            return 0;
        }
    }

    private String getChildText(Element el, String name) {
        NodeList nl = el.getElementsByTagName(name);
        if (nl.getLength() == 0) return null;
        return nl.item(0).getTextContent();
    }
}