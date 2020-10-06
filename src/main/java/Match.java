import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@AllArgsConstructor
@Setter
@Getter
public class Match implements Comparable<Match> {
    private long size;
    private String name;

    public int compareTo(Match o) {
        if (this.getSize() == o.getSize()) {
            return 0;
        } else if (this.getSize() > o.getSize()) {
            return 1;
        } else return -1;
    }

    @Override
    public String toString() {
        return "Size=" + this.getSize() +
                " bytes and the name of file: '" + this.getName() + '\'';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Match match = (Match) o;
        return Objects.equals(name, match.name);
    }
}
