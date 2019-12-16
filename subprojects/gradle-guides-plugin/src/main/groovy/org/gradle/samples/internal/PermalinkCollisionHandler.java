package org.gradle.samples.internal;

import org.gradle.api.Action;
import org.gradle.api.Task;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class PermalinkCollisionHandler {
    private final Collection<DefaultSample> samples;
    private Map<String, List<DefaultSample>> samplesGroupedByDuplicatedPermalink;

    PermalinkCollisionHandler(Collection<DefaultSample> samples) {
        this.samples = samples;
    }

    private Map<String, List<DefaultSample>> getSamplesGroupedByDuplicatedPermalink() {
        if (samplesGroupedByDuplicatedPermalink == null) {
            samplesGroupedByDuplicatedPermalink = samples.stream().collect(Collectors.groupingBy(it -> it.getPermalink().get(), Collectors.toList())).entrySet().stream().filter(it -> it.getValue().size() > 1).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        return samplesGroupedByDuplicatedPermalink;
    }

    private DefaultSample getSampleByInstallTaskName(String installTaskName) {
        return samples.stream().map(DefaultSample.class::cast).filter(it -> it.getInstallTaskName().equals(installTaskName)).findFirst().get();
    }

    private String createMessage(Map<String, List<DefaultSample>> samplesGroupedByPermalink) {
        StringBuilder builder = new StringBuilder("Permalinks collision detected among samples:");
        samplesGroupedByPermalink.forEach((permalink, samples) -> {
            builder.append(String.format("\n * The following samples are sharing permalink '%s':", permalink));
            samples.forEach(sample -> {
                builder.append(String.format("\n   - Sample '%s'", sample.getName()));
            });
        });
        return builder.toString();
    }

    private void throwException(String message) {
        throw new IllegalStateException(message);
    }

    @SuppressWarnings("Convert2Lambda") // Additional task actions are not supported to be lambdas
    Action<Task> throwOrWarnOnPermalinkCollision() {
        return new Action<Task>() {
            @Override
            public void execute(Task task) {
                DefaultSample sample = PermalinkCollisionHandler.this.getSampleByInstallTaskName(task.getName());
                String permalink = sample.getPermalink().get();

                Map<String, List<DefaultSample>> samplesGroupedByPermalink = new HashMap<>();
                PermalinkCollisionHandler.this.getSamplesGroupedByDuplicatedPermalink().computeIfPresent(permalink, samplesGroupedByPermalink::put);
                if (!samplesGroupedByPermalink.isEmpty()) {
                    boolean hasPermalinkCollisionInExecutionPlan = samplesGroupedByPermalink.values().stream().flatMap(Collection::stream).filter(it -> task.getProject().getGradle().getTaskGraph().hasTask(task.getProject().getTasks().getByName(it.getInstallTaskName()))).count() > 1;
                    if (hasPermalinkCollisionInExecutionPlan) {
                        PermalinkCollisionHandler.this.throwException(PermalinkCollisionHandler.this.createMessage(samplesGroupedByPermalink));
                    } else {
                        task.getLogger().warn(PermalinkCollisionHandler.this.createMessage(samplesGroupedByPermalink));
                    }
                }
            }
        };
    }

    @SuppressWarnings("Convert2Lambda") // Additional task actions are not supported to be lambdas
    Action<Task> throwOnPermalinkCollision() {
        return new Action<Task>() {
            @Override
            public void execute(Task t) {
                Map<String, List<DefaultSample>> samplesGroupedByPermalink = PermalinkCollisionHandler.this.getSamplesGroupedByDuplicatedPermalink();
                if (!samplesGroupedByPermalink.isEmpty()) {
                    PermalinkCollisionHandler.this.throwException(PermalinkCollisionHandler.this.createMessage(samplesGroupedByPermalink));
                }
            }
        };
    }
}
