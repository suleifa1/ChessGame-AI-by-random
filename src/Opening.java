import java.util.List;

public class Opening {
    private String name;
    private List<String> moves;

    public Opening(String name, List<String> moves) {
        this.name = name;
        this.moves = moves;
    }

    public String getName() {
        return name;
    }

    public List<String> getMoves() {
        return moves;
    }
}
