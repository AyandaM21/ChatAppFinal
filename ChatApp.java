import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class ChatApp {

    private static String storedFirstName = "";
    private static String storedLastName = "";
    private static String storedUsername = "";
    private static String storedPassword = "";
    private static String storedCellNumber = "";
    private static boolean isUserRegistered = false;

    public static boolean checkUserName(String username) {
        return username.contains("_") && username.length() <= 5 && username.length() > 0;
    }

    public static boolean checkPasswordComplexity(String password) {
        String pattern = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+]).{8,}$";
        return Pattern.matches(pattern, password);
    }

    private static boolean checkCellPhoneNumber(String cellNumber) {
        return Pattern.matches("^\\+27735022948", cellNumber);
    }

    public static String registerUser(String firstName, String lastName,
                                      String username, String password,
                                      String cellNumber) {
        if (!checkUserName(username)) {
            return "Username must contain '_' and be ≤5 characters.";
        }
        if (!checkPasswordComplexity(password)) {
            return "Password must be 8+ chars, include uppercase, digit, special char.";
        }
        if (!checkCellPhoneNumber(cellNumber)) {
            return "Cell number must start with +27 and have 9 digits.";
        }
        storedFirstName = firstName;
        storedLastName = lastName;
        storedUsername = username;
        storedPassword = password;
        storedCellNumber = cellNumber;
        isUserRegistered = true;
        return "Registration successful!";
    }

    public static boolean loginUser(String username, String password) {
        if (!isUserRegistered) return false;
        return storedUsername.equals(username) && storedPassword.equals(password);
    }

    public static String returnLoginStatus(String username, String password) {
        if (loginUser(username, password)) {
            return "Welcome " + storedFirstName + " " + storedLastName + "!";
        } else {
            return "Username or password incorrect.";
        }
    }

    // ---------- Message class (public static for tests) ----------
    public static class Message {
        public String messageID;
        public int messageNumber;
        public String recipient;
        public String messageText;
        public String messageHash;
        public boolean sent;

        public Message(String messageID, int messageNumber, String recipient, String messageText, String messageHash) {
            this.messageID = messageID;
            this.messageNumber = messageNumber;
            this.recipient = recipient;
            this.messageText = messageText;
            this.messageHash = messageHash;
            this.sent = false;
        }
    }

    // ---------- Storage arrays (public static for tests) ----------
    public static final List<Message> allMessages = new ArrayList<>();
    public static int totalSentCount = 0;
    private static final Random random = new Random();

    public static final ArrayList<String> sentMessagesArray = new ArrayList<>();
    public static final ArrayList<String> disregardedMessagesArray = new ArrayList<>();
    public static final ArrayList<String> storedMessagesArray = new ArrayList<>();
    public static final ArrayList<String> messageHashArray = new ArrayList<>();
    public static final ArrayList<String> messageIDArray = new ArrayList<>();

    // ---------- Helper methods ----------
    public static boolean checkMessageID(String messageID) {
        return messageID != null && messageID.length() == 10 && messageID.matches("\\d+");
    }

    public static String checkRecipientCell(String cellNumber) {
        if (cellNumber == null)
            return "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
        if (Pattern.matches("^\\+\\d{9,12}$", cellNumber))
            return "Cell phone number successfully captured.";
        else
            return "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
    }

    private static String generateMessageID() {
        return String.format("%010d", random.nextInt(1000000000) + 1000000000);
    }

    public static String createMessageHash(String messageID, int messageNumber, String messageText) {
        if (messageID == null || messageID.length() < 2) return "ERROR";
        String firstTwo = messageID.substring(0, 2);
        String[] words = messageText.trim().split("\\s+");
        if (words.length == 0) return firstTwo + ":" + messageNumber + ":";
        String firstWord = words[0].replaceAll("[^a-zA-Z]", "").toUpperCase();
        String lastWord = words[words.length - 1].replaceAll("[^a-zA-Z]", "").toUpperCase();
        return firstTwo + ":" + messageNumber + ":" + firstWord + lastWord;
    }

    public static String sentMessage(Scanner scanner, Message msg) {
        System.out.println("\nWhat would you like to do with this message?");
        System.out.println("1. Send Message");
        System.out.println("2. Disregard Message");
        System.out.println("3. Store Message to send later");
        System.out.print("Choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        messageIDArray.add(msg.messageID);
        messageHashArray.add(msg.messageHash);

        switch (choice) {
            case 1:
                msg.sent = true;
                totalSentCount++;
                allMessages.add(msg);
                sentMessagesArray.add(msg.messageText);
                return "Message successfully sent.";
            case 2:
                disregardedMessagesArray.add(msg.messageText);
                return "Press 0 to delete the message.";
            case 3:
                msg.sent = false;
                allMessages.add(msg);
                storeMessage(msg);
                storedMessagesArray.add(msg.messageText);
                return "Message successfully stored.";
            default:
                return "Invalid option. Message disregarded.";
        }
    }

    public static String printMessages() {
        if (allMessages.isEmpty()) return "No messages have been created yet.";
        StringBuilder sb = new StringBuilder();
        for (Message msg : allMessages) {
            sb.append("Message ID: ").append(msg.messageID)
              .append(", Hash: ").append(msg.messageHash)
              .append(", Recipient: ").append(msg.recipient)
              .append(", Message: ").append(msg.messageText)
              .append("\n");
        }
        return sb.toString();
    }

    public static int returnTotalMessages() {
        return totalSentCount;
    }

    public static void storeMessage(Message msg) {
        try (FileWriter fw = new FileWriter("messages.json", true)) {
            String json = String.format(
                "{\"messageID\":\"%s\",\"messageNumber\":%d,\"recipient\":\"%s\",\"message\":\"%s\",\"messageHash\":\"%s\",\"sent\":%b}\n",
                msg.messageID, msg.messageNumber, msg.recipient, msg.messageText, msg.messageHash, msg.sent
            );
            fw.write(json);
        } catch (IOException e) {
            System.out.println("Error storing message: " + e.getMessage());
        }
    }

    private static void loadAllMessagesFromFile() {
        File file = new File("messages.json");
        if (!file.exists()) return;

        allMessages.clear();
        sentMessagesArray.clear();
        disregardedMessagesArray.clear();
        storedMessagesArray.clear();
        messageHashArray.clear();
        messageIDArray.clear();
        totalSentCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String id = extractJsonValue(line, "messageID");
                String numStr = extractJsonValue(line, "messageNumber");
                String recipient = extractJsonValue(line, "recipient");
                String messageText = extractJsonValue(line, "message");
                String hash = extractJsonValue(line, "messageHash");
                String sentStr = extractJsonValue(line, "sent");

                if (id == null || numStr == null || recipient == null || messageText == null || hash == null)
                    continue;

                int msgNumber = Integer.parseInt(numStr);
                boolean sent = Boolean.parseBoolean(sentStr);

                Message msg = new Message(id, msgNumber, recipient, messageText, hash);
                msg.sent = sent;
                allMessages.add(msg);
                messageIDArray.add(id);
                messageHashArray.add(hash);

                if (sent) {
                    sentMessagesArray.add(messageText);
                    totalSentCount++;
                } else {
                    storedMessagesArray.add(messageText);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading stored messages: " + e.getMessage());
        }
    }

    private static String extractJsonValue(String line, String key) {
        String search = "\"" + key + "\":\"";
        int start = line.indexOf(search);
        if (start == -1) {
            if (key.equals("sent")) {
                search = "\"" + key + "\":";
                start = line.indexOf(search);
                if (start != -1) {
                    int end = line.indexOf(",", start);
                    if (end == -1) end = line.indexOf("}", start);
                    if (end != -1)
                        return line.substring(start + search.length(), end).trim();
                }
            }
            return null;
        }
        start += search.length();
        int end = line.indexOf("\"", start);
        if (end == -1) return null;
        return line.substring(start, end);
    }

    private static void storedMessagesMenu(Scanner scanner) {
        boolean back = false;
        while (!back) {
            System.out.println("\n===== STORED MESSAGES =====");
            System.out.println("a. Display sender & recipient of all stored messages");
            System.out.println("b. Display the longest stored message");
            System.out.println("c. Search for a message ID → show recipient & message");
            System.out.println("d. Search all messages for a particular recipient");
            System.out.println("e. Delete a message using its hash");
            System.out.println("f. Display full report (hash, recipient, message)");
            System.out.println("0. Back to main menu");
            System.out.print("Your choice: ");
            String opt = scanner.nextLine().toLowerCase();

            switch (opt) {
                case "a": displaySenderRecipientAllStored(); break;
                case "b": displayLongestStoredMessage(); break;
                case "c": searchMessageById(scanner); break;
                case "d": searchByRecipient(scanner); break;
                case "e": deleteByHash(scanner); break;
                case "f": displayFullReport(); break;
                case "0": back = true; break;
                default: System.out.println("Invalid option. Try again.");
            }
        }
    }

    private static String getSenderName() {
        return storedFirstName + " " + storedLastName;
    }

    private static void displaySenderRecipientAllStored() {
        if (storedMessagesArray.isEmpty()) {
            System.out.println("No stored messages found.");
            return;
        }
        System.out.println("\n--- All Stored Messages (Sender & Recipient) ---");
        for (Message msg : allMessages) {
            if (!msg.sent && storedMessagesArray.contains(msg.messageText))
                System.out.println("Sender: " + getSenderName() + " | Recipient: " + msg.recipient);
        }
    }

    private static void displayLongestStoredMessage() {
        if (storedMessagesArray.isEmpty()) {
            System.out.println("No stored messages.");
            return;
        }
        String longest = "";
        for (String msg : storedMessagesArray)
            if (msg.length() > longest.length()) longest = msg;
        System.out.println("Longest stored message: \"" + longest + "\" (length: " + longest.length() + ")");
    }

    private static void searchMessageById(Scanner scanner) {
        System.out.print("Enter Message ID (10 digits): ");
        String id = scanner.nextLine();
        for (Message msg : allMessages) {
            if (msg.messageID.equals(id)) {
                System.out.println("Recipient: " + msg.recipient);
                System.out.println("Message: " + msg.messageText);
                return;
            }
        }
        System.out.println("Message ID not found.");
    }

    private static void searchByRecipient(Scanner scanner) {
        System.out.print("Enter recipient phone number (e.g., +27838884567): ");
        String phone = scanner.nextLine();
        boolean found = false;
        for (Message msg : allMessages) {
            if (msg.recipient.equals(phone)) {
                System.out.println("Message: " + msg.messageText);
                found = true;
            }
        }
        if (!found) System.out.println("No messages for recipient " + phone);
    }

    private static void deleteByHash(Scanner scanner) {
        System.out.print("Enter message hash to delete: ");
        String hash = scanner.nextLine();
        Iterator<Message> iterator = allMessages.iterator();
        boolean removed = false;
        while (iterator.hasNext()) {
            Message msg = iterator.next();
            if (msg.messageHash.equals(hash)) {
                storedMessagesArray.remove(msg.messageText);
                sentMessagesArray.remove(msg.messageText);
                disregardedMessagesArray.remove(msg.messageText);
                messageHashArray.remove(hash);
                messageIDArray.remove(msg.messageID);
                if (msg.sent) totalSentCount--;
                iterator.remove();
                removed = true;
                System.out.println("Message: \"" + msg.messageText + "\" successfully deleted.");
                break;
            }
        }
        if (removed) rewriteJsonFile();
        else System.out.println("No message found with that hash.");
    }

    private static void rewriteJsonFile() {
        try (FileWriter fw = new FileWriter("messages.json", false)) {
            for (Message msg : allMessages) {
                String json = String.format(
                    "{\"messageID\":\"%s\",\"messageNumber\":%d,\"recipient\":\"%s\",\"message\":\"%s\",\"messageHash\":\"%s\",\"sent\":%b}\n",
                    msg.messageID, msg.messageNumber, msg.recipient, msg.messageText, msg.messageHash, msg.sent
                );
                fw.write(json);
            }
        } catch (IOException e) {
            System.out.println("Error updating messages file: " + e.getMessage());
        }
    }

    private static void displayFullReport() {
        System.out.println("\n===== FULL STORED MESSAGES REPORT =====");
        System.out.printf("%-15s %-20s %-50s%n", "Message Hash", "Recipient", "Message");
        System.out.println("--------------------------------------------------------------------------------");
        for (Message msg : allMessages) {
            if (!msg.sent) {
                System.out.printf("%-15s %-20s %-50s%n", msg.messageHash, msg.recipient, msg.messageText);
            }
        }
    }

    private static void startMessagingSession(Scanner scanner) {
        System.out.println("\nWelcome to QuickChat.");
        loadAllMessagesFromFile();

        boolean sessionActive = true;
        while (sessionActive) {
            System.out.println("\n--- QuickChat Menu ---");
            System.out.println("1. Send Messages");
            System.out.println("2. Show recently sent messages");
            System.out.println("3. Stored Messages");
            System.out.println("4. Quit");
            System.out.print("Choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1: sendMessagesFlow(scanner); break;
                case 2: System.out.println("Coming Soon."); break;
                case 3: storedMessagesMenu(scanner); break;
                case 4: sessionActive = false; System.out.println("Exiting QuickChat. Goodbye!"); break;
                default: System.out.println("Invalid option. Please try again.");
            }
        }
        System.out.println("Total messages sent during this session: " + returnTotalMessages());
    }

    private static void sendMessagesFlow(Scanner scanner) {
        System.out.print("How many messages do you want to enter? ");
        int numMessages = scanner.nextInt();
        scanner.nextLine();
        int messageCounter = 0;
        for (int i = 0; i < numMessages; i++) {
            System.out.println("\n--- New Message " + (i + 1) + " ---");
            String recipient;
            while (true) {
                System.out.print("Recipient cell number (e.g., +27718693002): ");
                recipient = scanner.nextLine();
                String result = checkRecipientCell(recipient);
                if (result.equals("Cell phone number successfully captured.")) break;
                else System.out.println(result);
            }
            String messageText;
            while (true) {
                System.out.print("Message (max 250 characters): ");
                messageText = scanner.nextLine();
                if (messageText.length() <= 250) {
                    System.out.println("Message ready to send.");
                    break;
                } else {
                    int excess = messageText.length() - 250;
                    System.out.println("Message exceeds " + excess + " characters; please reduce the size.");
                }
            }
            String msgID = generateMessageID();
            messageCounter++;
            String msgHash = createMessageHash(msgID, messageCounter, messageText);
            Message msg = new Message(msgID, messageCounter, recipient, messageText, msgHash);
            String actionResult = sentMessage(scanner, msg);
            System.out.println(actionResult);
            if (actionResult.equals("Message successfully sent.") || actionResult.equals("Message successfully stored.")) {
                System.out.println("\nMessage Details:");
                System.out.println("Message ID: " + msg.messageID);
                System.out.println("Message Hash: " + msg.messageHash);
                System.out.println("Recipient: " + msg.recipient);
                System.out.println("Message: " + msg.messageText);
            }
        }
        System.out.println("\nTotal number of messages sent: " + returnTotalMessages());
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            int choice;
            System.out.println("===============================");
            System.out.println("       WELCOME TO CHAT APP      ");
            System.out.println("===============================");
            do {
                System.out.println("\n--- MAIN MENU ---");
                System.out.println("1. Register");
                System.out.println("2. Login");
                System.out.println("3. Exit");
                System.out.print("Choice: ");
                while (!scanner.hasNextInt()) {
                    System.out.print("Enter 1, 2, or 3: ");
                    scanner.next();
                }
                choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1 -> {
                        System.out.println("\n--- REGISTRATION ---");
                        System.out.print("First name: ");
                        String fn = scanner.nextLine();
                        System.out.print("Last name: ");
                        String ln = scanner.nextLine();
                        System.out.print("Username (with _, max 5): ");
                        String un = scanner.nextLine();
                        System.out.print("Password (8+ chars, uppercase, digit, special): ");
                        String pw = scanner.nextLine();
                        System.out.print("Cell number (+27xxxxxxxxx): ");
                        String cell = scanner.nextLine();
                        String result = registerUser(fn, ln, un, pw, cell);
                        System.out.println("\nResult: " + result);
                        if (result.equals("Registration successful!")) {
                            System.out.println("✓ Username captured.");
                            System.out.println("✓ Password captured.");
                            System.out.println("✓ Cell number captured.");
                        }
                    }
                    case 2 -> {
                        System.out.println("\n--- LOGIN ---");
                        if (!isUserRegistered) {
                            System.out.println("No user registered. Please register first.");
                            break;
                        }
                        System.out.print("Username: ");
                        String loginU = scanner.nextLine();
                        System.out.print("Password: ");
                        String loginP = scanner.nextLine();
                        String message = returnLoginStatus(loginU, loginP);
                        System.out.println("\n" + message);
                        if (loginUser(loginU, loginP)) {
                            startMessagingSession(scanner);
                        }
                    }
                    case 3 -> System.out.println("\nGoodbye!");
                    default -> System.out.println("Invalid choice. Try again.");
                }
            } while (choice != 3);
        }
    }
}