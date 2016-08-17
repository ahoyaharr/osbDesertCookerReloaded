import org.osbot.rs07.script.Script;
import java.awt.*;

public class Cursor {
    private final BasicStroke TWO_STROKE = new BasicStroke(1.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);
    private final BasicStroke FIVE_STROKE = new BasicStroke(5);
    private final Color BLACK_COLOR = new Color(255, 255, 255);
    private final Color WHITE_COLOR = new Color(211, 81, 26, 170);
    private Script s;

    public Cursor(Script s) {
        this.s = s;
    }

    public void draw(final Graphics2D g) {
        final Point point = s.mouse.getPosition();
        final int x = point.x;
        final int y = point.y;
        g.setColor(WHITE_COLOR);
        g.setStroke(TWO_STROKE);
        g.drawLine(x - 10, y + 10, x + 10, y - 10);
        g.drawLine(x + 10, y + 10, x - 10, y - 10);
        g.setColor(Color.WHITE);
    }
}