package ch.admin.bit.jeap.testorchestrator.adapter.zephyr;

import lombok.*;

import java.util.List;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
public class ZephyrTestRunDto {
    @NonNull
    String projectKey;
    @NonNull
    String name;
    @NonNull
    @Singular("items")
    List<ZephyrItemDto> items;
}
