package com.skjolberg.nfc.command.acr1281;


public class ExclusiveModeConfiguration {

    public static enum ExclusiveMode {
        SHARE(0x00),
        EXCLUSIVE(0x01);

        private ExclusiveMode(int value) {
            this.value = value;
        }

        private final int value;

        public int getValue() {
            return value;
        }

        public static ExclusiveMode parse(int value) {
            for (ExclusiveMode pollingInterval : values()) {
                if (value == pollingInterval.getValue()) {
                    return pollingInterval;
                }
            }
            throw new IllegalArgumentException();
        }

        public byte[] getData() {
            byte[] data = new byte[1];

            data[0] = (byte) (value);

            return data;
        }

    }

    private ExclusiveMode modeConfiguration;
    private ExclusiveMode currentModeConfiguration;

    public ExclusiveModeConfiguration(byte[] data) {
        modeConfiguration = ExclusiveMode.parse(data[0]);
        currentModeConfiguration = ExclusiveMode.parse(data[1]);
    }

    public ExclusiveMode getModeConfiguration() {
        return modeConfiguration;
    }

    public void setModeConfiguration(ExclusiveMode modeConfiguration) {
        this.modeConfiguration = modeConfiguration;
    }

    public ExclusiveMode getCurrentModeConfiguration() {
        return currentModeConfiguration;
    }

    public void setCurrentModeConfiguration(ExclusiveMode currentModeConfiguration) {
        this.currentModeConfiguration = currentModeConfiguration;
    }


    public byte[] getData() {
        return new byte[]{(byte) modeConfiguration.getValue(), (byte) currentModeConfiguration.getValue()};
    }

}
