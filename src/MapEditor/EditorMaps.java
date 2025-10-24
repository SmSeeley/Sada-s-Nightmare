package MapEditor;

import Level.Map;
import Maps.FirstRoom;
import Maps.SecondRoom;
import Maps.TestMap;
import Maps.ThirdRoomDungeon;
import Maps.TitleScreenMap;

import java.util.ArrayList;

public class EditorMaps {
    public static ArrayList<String> getMapNames() {
        return new ArrayList<String>() {{
            add("TestMap");
            add("TitleScreen");
            add("SecondRoom");
            add("FirstRoom");
            add("ThirdRoomDungeon");
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
            default:
                throw new RuntimeException("Unrecognized map name");
        }
    }
}
