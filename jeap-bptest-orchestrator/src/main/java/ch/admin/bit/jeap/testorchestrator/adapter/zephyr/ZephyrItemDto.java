package ch.admin.bit.jeap.testorchestrator.adapter.zephyr;

import lombok.*;

import java.util.List;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
public class ZephyrItemDto {
    @NonNull
    String testCaseKey;
    @NonNull
    String status;
    @NonNull
    String environment;
    @NonNull
    String comment;

    @Singular("scriptResult")
    List<ZephyrStepDto> scriptResults;

}
