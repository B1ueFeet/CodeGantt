package com.bf.Service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import com.bf.Model.Dependency;
import com.bf.Model.Task;
import com.bf.Repository.DependencyRepository;
import com.bf.Repository.TaskRepository;

import java.util.*;
import java.util.UUID;

@ApplicationScoped
public class DependencyService {

    private final DependencyRepository deps;
    private final TaskRepository tasks;

    public DependencyService(DependencyRepository deps, TaskRepository tasks) {
        this.deps = deps;
        this.tasks = tasks;
    }

    @Transactional
    public Dependency create(UUID projectId, UUID predecessorId, UUID successorId, int lagMinutes) {
        var pred = tasks.findById(predecessorId);
        var succ = tasks.findById(successorId);
        if (pred == null || succ == null) {
            throw new IllegalArgumentException("Tarea no existe");
        }
        if (!pred.project.id.equals(projectId) || !succ.project.id.equals(projectId)) {
            throw new IllegalArgumentException("Ambas tareas deben pertenecer al mismo proyecto");
        }

        // validate FS rule: successor.startAt >= predecessor.endAt + lag
        if (succ.startAt.isBefore(pred.endAt.plusMinutes(lagMinutes))) {
            throw new IllegalArgumentException("Dependencia FS violaría fechas");
        }

        // detect cycles: ensure there's no path from successor -> predecessor
        if (hasPath(succ.id, pred.id)) {
            throw new IllegalArgumentException("Crear esta dependencia generaría un ciclo");
        }

        var d = new Dependency();
        d.id = UUID.randomUUID();
        d.project = pred.project;
        d.predecessor = pred;
        d.successor = succ;
        d.lagMinutes = lagMinutes;
        d.type = "FS";
        deps.persist(d);
        return d;
    }

    public boolean hasPath(UUID startTaskId, UUID targetTaskId) {
        var visited = new HashSet<UUID>();
        var stack = new ArrayDeque<UUID>();
        stack.push(startTaskId);
        while (!stack.isEmpty()) {
            var cur = stack.pop();
            if (!visited.add(cur)) continue;
            if (cur.equals(targetTaskId)) return true;
            var successors = deps.listSuccessors(cur);
            for (var s : successors) {
                stack.push(s.successor.id);
            }
        }
        return false;
    }

    @Transactional
    public void delete(UUID id) {
        boolean deleted = deps.deleteById(id);
        if (!deleted) throw new IllegalArgumentException("Dependencia no existe");
    }
}
