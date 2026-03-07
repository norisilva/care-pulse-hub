package com.custodyreport.backend.neutralization.availability;

import org.springframework.stereotype.Component;

@Component
public class HardwareProbe {

    public long getAvailableRamMb() {
        Runtime runtime = Runtime.getRuntime();
        long freeMemory = runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        return (maxMemory - totalMemory + freeMemory) / (1024 * 1024);
    }

    public boolean hasEnoughRamForNlp() {
        return getAvailableRamMb() >= 512;
    }

    public boolean hasEnoughRamForWord2Vec() {
        return getAvailableRamMb() >= 1024;
    }
}
