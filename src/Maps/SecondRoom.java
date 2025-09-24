//Creates the second room that the user will walk into

package Maps;

import Level.Map;
import Tilesets.CommonTileset;
import Utils.Point;

public class SecondRoom extends Map {
    public SecondRoom() {
        super("SecondRoom.txt", new CommonTileset());
        this.playerStartPosition = new Point(1, 11);
    }
}