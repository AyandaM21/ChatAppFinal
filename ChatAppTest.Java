import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class ChatAppTest {

    @BeforeEach
    public void setUp() {
        ChatApp.sentMessagesArray.clear();
        ChatApp.disregardedMessagesArray.clear();
        ChatApp.storedMessagesArray.clear();
        ChatApp.messageHashArray.clear();
        ChatApp.messageIDArray.clear();
        ChatApp.allMessages.clear();
        ChatApp.totalSentCount = 0;
    }

    @Test
    public void testSentMessagesArray() {
        ChatApp.sentMessagesArray.add("Did you get the cake?");
        ChatApp.sentMessagesArray.add("It is dinner time!");
        assertTrue(ChatApp.sentMessagesArray.contains("Did you get the cake?"));
        assertTrue(ChatApp.sentMessagesArray.contains("It is dinner time!"));
    }

    @Test
    public void testLongestMessage() {
        List<String> messages = Arrays.asList(
            "Did you get the cake?",
            "Where are you? You are late! I have asked you to be on time.",
            "Yohoooo, I am at your gate.",
            "It is dinner time!"
        );
        String longest = messages.stream()
            .max(Comparator.comparingInt(String::length))
            .orElse("");
        assertEquals("Where are you? You are late! I have asked you to be on time.", longest);
    }

    @Test
    public void testSearchByMessageId() {
        ChatApp.Message msg = new ChatApp.Message("1234567890", 4, "0838884567", "It is dinner time!", "hash123");
        ChatApp.allMessages.add(msg);
        String foundMsg = null;
        for (ChatApp.Message m : ChatApp.allMessages) {
            if (m.messageID.equals("1234567890")) {
                foundMsg = m.messageText;
                break;
            }
        }
        assertEquals("It is dinner time!", foundMsg);
    }

    @Test
    public void testSearchByRecipient() {
        ChatApp.Message msg2 = new ChatApp.Message("id2", 2, "+27838884567",
            "Where are you? You are late! I have asked you to be on time.", "hash2");
        ChatApp.Message msg5 = new ChatApp.Message("id5", 5, "+27838884567",
            "Ok, I am leaving without you.", "hash5");
        ChatApp.allMessages.add(msg2);
        ChatApp.allMessages.add(msg5);
        List<String> results = new ArrayList<>();
        for (ChatApp.Message m : ChatApp.allMessages) {
            if (m.recipient.equals("+27838884567")) {
                results.add(m.messageText);
            }
        }
        assertEquals(2, results.size());
        assertTrue(results.contains("Where are you? You are late! I have asked you to be on time."));
        assertTrue(results.contains("Ok, I am leaving without you."));
    }

    @Test
    public void testDeleteByHash() {
        ChatApp.Message msg2 = new ChatApp.Message("id2", 2, "+27838884567",
            "Where are you? You are late! I have asked you to be on time.", "hashToDelete");
        ChatApp.allMessages.add(msg2);
        ChatApp.storedMessagesArray.add(msg2.messageText);
        boolean removed = ChatApp.allMessages.removeIf(m -> m.messageHash.equals("hashToDelete"));
        if (removed) {
            ChatApp.storedMessagesArray.remove(msg2.messageText);
        }
        assertTrue(removed);
        assertEquals(0, ChatApp.allMessages.size());
    }

    @Test
    public void testDisplayReport() {
        ChatApp.Message stored = new ChatApp.Message("id99", 99, "+27711223344", "Hello world", "hash99");
        stored.sent = false;
        ChatApp.allMessages.add(stored);
        ChatApp.storedMessagesArray.add("Hello world");
        boolean found = false;
        for (ChatApp.Message m : ChatApp.allMessages) {
            if (!m.sent && m.messageText.equals("Hello world")) {
                found = true;
                break;
            }
        }
        assertTrue(found);
        assertTrue(ChatApp.storedMessagesArray.contains("Hello world"));
    }
}
