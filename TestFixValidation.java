import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Simple validation script to demonstrate the UX fix logic
 * This simulates the key improvement we made to the schema generation flow
 */
public class TestFixValidation {
    
    public static void main(String[] args) {
        System.out.println("🧪 GraphQL Schema Generation UX Fix Validation");
        System.out.println("===============================================");
        
        // Simulate the key method we added to SdlSchemaGenerator
        String fileName = "schema_graphql.graphql";
        boolean shouldProceed = checkFileConflictsAndGetUserConsent(fileName);
        
        if (shouldProceed) {
            System.out.println("✅ User chose to proceed - schema generation would continue");
        } else {
            System.out.println("🛑 User chose to cancel - NO processing, NO unwanted files!");
            System.out.println("🎯 This is the key improvement: early exit respects user choice");
        }
    }
    
    /**
     * This method simulates the key function we added to fix the UX issue
     * It checks for file conflicts BEFORE doing any processing
     */
    private static boolean checkFileConflictsAndGetUserConsent(String fileName) {
        Path filePath = Paths.get(fileName);
        
        if (Files.exists(filePath)) {
            System.out.println("⚠️  File already exists: " + fileName);
            System.out.print("❓ Do you want to overwrite it? [y/N]: ");
            
            Scanner scanner = new Scanner(System.in);
            String response = scanner.nextLine().trim();
            
            if (!response.equalsIgnoreCase("y") && !response.equalsIgnoreCase("yes")) {
                System.out.println("📝 Schema generation cancelled by user.");
                return false;
            }
        }
        
        return true;
    }
}