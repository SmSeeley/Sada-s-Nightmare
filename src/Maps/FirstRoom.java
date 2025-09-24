//Map that user will walk into
package Maps;

import Level.Map;
import Tilesets.CommonTileset;
import Utils.Point;

public class FirstRoom extends Map {
    public FirstRoom() {
        super("FirstRoom.txt", new CommonTileset());
        this.playerStartPosition = new Point(1, 11);
    }
}