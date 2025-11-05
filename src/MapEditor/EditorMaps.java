package MapEditor;

import Level.Map;
import Maps.*;

import java.util.ArrayList;

public class EditorMaps {
    public static ArrayList<String> getMapNames() {
        return new ArrayList<String>() {{
            add("TestMap");
            add("TitleScreen");
            add("SecondRoom");
            add("FirstRoom");
            add("ThirdRoomDungeon");
            add("Room4Dungeon");
            add("Room5Dungeon");
            add("TheHub1");
            add("Desert_1");
            add("Winter_1");
            add("Desert_2");
            add("Desert_3");
            add("Desert_4");
            add("Desert_5");
            add("Winter_2");
            add("Winter_3");
            add("Winter_4");
            add("Winter_5");
            add("Fire_1");
            add("Fire_2");
            add("Fire_3");
            add("Fire_4");
            add("Fire_5");
        }};
    }

    public static Map getMapByName(String mapName) {
        switch(mapName) {
            case "TestMap":
                return new TestMap();
            case "TitleScreen":
                return new TitleScreenMap();
            case "SecondRoom":
                return new SecondRoom();
            case "FirstRoom":
                return new FirstRoom();
            case "ThirdRoomDungeon":
                return new ThirdRoomDungeon();
            case "Room4Dungeon":
                return new Room4Dungeon();
            case "Room5Dungeon":
                return new Room5Dungeon();
            case "TheHub1":
                return new TheHub1();
            case "Desert_1":
                return new Desert_1();
            case "Winter_1":
                return new Winter_1();
             case "Desert_2":
                return new Desert_2();
            case "Desert_3":
                return new Desert_3();
            case "Desert_4":
                return new Desert_4();
            case "Desert_5":
                return new Desert_5();
            case "Winter_2":
                return new Winter_2();
            case "Winter_3":
                return new Winter_3();
            case "Winter_4":
                return new Winter_4();
            case "Winter_5":
                return new Winter_5();
            case "Fire_1":
                return new Fire_1();
            case "Fire_2":
                return new Fire_2();
            case "Fire_3":
                return new Fire_3();
            case "Fire_4":
                return new Fire_4();
            case "Fire_5":
                return new Fire_5();
            default:
                throw new RuntimeException("Unrecognized map name");
        }
    }
}
