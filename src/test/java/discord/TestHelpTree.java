package discord;

import static org.junit.Assert.*;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
/*
public class TestHelpTree {
    @Test
    public void getNextTextNodeListByText_metodReturnsRightValue() throws IOException {
        TreeManager helpTree = new TreeManager(new TreeRoot("src/main/resources/dialog-starter.yaml"));
        var actVal1 = helpTree.getNextTextNodeListByText("question1").toArray(new String[0]);
        var expVal1 = List.of("question2", "question7").toArray(new String[0]);
        assertArrayEquals(expVal1, actVal1);
        var actVal2 = helpTree.getNextTextNodeListByText("question2").toArray(new String[0]);
        var expVal2 = List.of("question3", "question6").toArray(new String[0]);
        assertArrayEquals(expVal2, actVal2);
        var actVal3 = helpTree.getNextTextNodeListByText("question3").toArray(new String[0]);
        var expVal3 = List.of("question4", "question5").toArray(new String[0]);
        assertArrayEquals(expVal3, actVal3);
        var actVal4 = helpTree.getNextTextNodeListByText("question4").toArray(new String[0]);
        var expVal4 = List.of("answer1").toArray(new String[0]);
        assertArrayEquals(expVal4, actVal4);
        var actList = helpTree.getNextTextNodeListByText("answer1");
        assertNull(actList);
    }

    @Test
    public void getNextTextNodeListByText_returnNullOnWrongText() throws IOException {
        TreeManager helpTree = new TreeManager(new TreeRoot("src/main/resources/dialog-starter.yaml"));
        var actList = helpTree.getNextTextNodeListByText("wrong_text");
        assertNull(actList);
    }
}
*/