package dev.hen.client.util;

public class Setting {
    public final String name;
    public final boolean isBool;
    public final String[] options; // non-null = mode setting
    public double value, min, max, step;

    private Setting(String name, boolean isBool, String[] options, double value, double min, double max, double step) {
        this.name = name; this.isBool = isBool; this.options = options;
        this.value = value; this.min = min; this.max = max; this.step = step;
    }

    public static Setting bool(String name, boolean def) { return new Setting(name, true, null, def ? 1 : 0, 0, 1, 1); }
    public static Setting number(String name, double def, double min, double max, double step) {
        return new Setting(name, false, null, def, min, max, step);
    }
    public static Setting mode(String name, int def, String... options) {
        return new Setting(name, false, options, def, 0, options.length - 1, 1);
    }

    public boolean getBool() { return value > 0.5; }
    public double getNum() { return value; }
    public int getMode() { return (int) value; }
    public void toggle() { value = getBool() ? 0 : 1; }

    public void adjust(int dir) {
        if (options != null) { value = (getMode() + dir + options.length) % options.length; return; }
        value = Math.max(min, Math.min(max, Math.round((value + dir * step) * 1000.0) / 1000.0));
    }

    public String display() {
        if (isBool) return getBool() ? "ON" : "OFF";
        if (options != null) return options[getMode()];
        return String.valueOf(value);
    }
}
