package ch.admin.bit.jeap.testorchestrator.adapter.zephyr;

import lombok.*;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Builder
public class ZephyrStepDto {
    int index;
    @NonNull
    String status;

    String comment;
}
