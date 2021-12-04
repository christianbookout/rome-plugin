package romeplugin.newtitle;

public enum Title {
    TRIBUNE, SENATOR, MAYOR, JUDGE, CONSOLE, SENSOR, POPE, BUILDER, CITIZEN;

    public static Title getTitle(String title) {
        return Title.valueOf(title.toUpperCase());
    }
}
