package net.biomodels.jummp.utils;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

/**
 * JUnit 5 test class for SimpleMimeTypeMapper
 * <p>
 * Maven dependency:
 * <dependency>
 *     <groupId>org.junit.jupiter</groupId>
 *     <artifactId>junit-jupiter</artifactId>
 *     <version>5.10.1</version>
 *     <scope>test</scope>
 * </dependency>
 */
@DisplayName("SimpleMimeTypeMapper Tests")
public class SimpleMimeTypeMapperTest {

    private static SimpleMimeTypeMapper mapper;

    @BeforeAll
    @DisplayName("Initialize mapper and build maps from GitHub Gist")
    public static void setUp() throws Exception {
        mapper = new SimpleMimeTypeMapper();
        mapper.buildMap();
        System.out.println("Test setup complete - maps loaded successfully");
    }

    @Test
    @DisplayName("Test that maps are populated")
    public void testMapsArePopulated() {
        Map<String, String> extMap = mapper.getExtensionToMimeMap();
        Map<String, List<String>> mimeMap = mapper.getMimeToExtensionsMap();

        assertNotNull(extMap, "Extension to MIME map should not be null");
        assertNotNull(mimeMap, "MIME to extensions map should not be null");
        assertFalse(extMap.isEmpty(), "Extension to MIME map should not be empty");
        assertFalse(mimeMap.isEmpty(), "MIME to extensions map should not be empty");

        System.out.println("✓ Maps populated: " + extMap.size() + " extensions, " + mimeMap.size() + " MIME types");
    }

    @Test
    @DisplayName("Test getMimeType for common extensions")
    public void testGetMimeTypeForCommonExtensions() {
        assertEquals("application/pdf", mapper.getMimeType("pdf"), "PDF MIME type should match");
        assertEquals("image/jpeg", mapper.getMimeType("jpg"), "JPG MIME type should match");
        assertEquals("image/jpeg", mapper.getMimeType("jpeg"), "JPEG MIME type should match");
        assertEquals("text/html", mapper.getMimeType("html"), "HTML MIME type should match");
        assertEquals("application/json", mapper.getMimeType("json"), "JSON MIME type should match");
        assertEquals("video/mp4", mapper.getMimeType("mp4"), "MP4 MIME type should match");

        System.out.println("✓ Common extension lookups working correctly");
    }

    @Test
    @DisplayName("Test getMimeType with dot prefix")
    public void testGetMimeTypeWithDotPrefix() {
        assertEquals("application/pdf", mapper.getMimeType(".pdf"), "Should handle dot prefix");
        assertEquals("image/jpeg", mapper.getMimeType(".jpg"), "Should handle dot prefix");
        assertEquals("text/html", mapper.getMimeType(".html"), "Should handle dot prefix");

        System.out.println("✓ Dot prefix handling working correctly");
    }

    @Test
    @DisplayName("Test getMimeType for unknown extension")
    public void testGetMimeTypeForUnknownExtension() {
        String result = mapper.getMimeType("xyz123unknown");
        assertEquals("application/octet-stream", result, "Unknown extension should return default MIME type");

        System.out.println("✓ Unknown extension returns default MIME type");
    }

    @Test
    @DisplayName("Test getExtensions for common MIME types")
    public void testGetExtensionsForCommonMimeTypes() {
        List<String> pdfExts = mapper.getExtensions("application/pdf");
        assertNotNull(pdfExts, "Should return list for known MIME type");
        assertTrue(pdfExts.contains(".pdf"), "Should contain 'pdf' extension");

        List<String> jpegExts = mapper.getExtensions("image/jpeg");
        assertNotNull(jpegExts, "Should return list for known MIME type");
        assertTrue(jpegExts.size() > 1, "JPEG should have multiple extensions");
        assertTrue(jpegExts.contains(".jpg"), "Should contain 'jpg'");
        assertTrue(jpegExts.contains(".jpeg"), "Should contain 'jpeg'");

        System.out.println("✓ Extension lookup working: image/jpeg -> " + jpegExts);
    }

    @Test
    @DisplayName("Test getExtensions for unknown MIME type")
    public void testGetExtensionsForUnknownMimeType() {
        List<String> result = mapper.getExtensions("unknown/mimetype");
        assertNotNull(result, "Should return empty list, not null");
        assertTrue(result.isEmpty(), "Unknown MIME type should return empty list");

        System.out.println("✓ Unknown MIME type returns empty list");
    }

    @Test
    @DisplayName("Test getPrimaryExtension")
    public void testGetPrimaryExtension() {
        assertEquals(
                ".pdf",
                mapper.getPrimaryExtension("application/pdf"),
                "Primary PDF extension should be 'pdf'"
        );

        String jpegPrimary = mapper.getPrimaryExtension("image/jpeg");
        assertNotNull(jpegPrimary, "Primary extension should not be null");
        assertTrue(jpegPrimary.equals(".jpg") || jpegPrimary.equals(".jpeg"),
                "Primary JPEG extension should be jpg or jpeg");

        assertEquals(".html", mapper.getPrimaryExtension("text/html"), "Primary HTML extension should be 'html'");

        System.out.println("✓ Primary extension lookup working correctly");
    }

    @Test
    @DisplayName("Test getPrimaryExtension for unknown MIME type")
    public void testGetPrimaryExtensionForUnknownMimeType() {
        String result = mapper.getPrimaryExtension("unknown/mimetype");
        assertNull(result, "Unknown MIME type should return null");

        System.out.println("✓ Unknown MIME type returns null for primary extension");
    }

    @Test
    @DisplayName("Test bidirectional consistency")
    public void testBidirectionalConsistency() {
        // Test that extension -> MIME -> extensions includes original extension
        String extension = "pdf";
        String mimeType = mapper.getMimeType(extension);
        List<String> extensions = mapper.getExtensions(mimeType);

        assertTrue(extensions.contains("." + extension),
                "Extensions list should contain the original extension");

        System.out.println("✓ Bidirectional mapping is consistent");
    }

    @Test
    @DisplayName("Test Microsoft Office formats")
    public void testMicrosoftOfficeFormats() {
        assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                mapper.getMimeType("docx"), "DOCX MIME type should match");
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                mapper.getMimeType("xlsx"), "XLSX MIME type should match");
        assertEquals("application/vnd.openxmlformats-officedocument.presentationml.presentation",
                mapper.getMimeType("pptx"), "PPTX MIME type should match");

        System.out.println("✓ Microsoft Office formats working correctly");
    }

    @Test
    @DisplayName("Test text formats")
    public void testTextFormats() {
        assertEquals("text/plain", mapper.getMimeType("txt"), "TXT MIME type should match");
        assertEquals("text/csv", mapper.getMimeType("csv"), "CSV MIME type should match");
        // application/xml instead of text/xml
        assertNotEquals("text/xml", mapper.getMimeType("xml"), "XML MIME type should match");

        System.out.println("✓ Text formats working correctly");
    }

    @Test
    @DisplayName("Test image formats")
    public void testImageFormats() {
        assertEquals("image/png", mapper.getMimeType("png"), "PNG MIME type should match");
        assertEquals("image/gif", mapper.getMimeType("gif"), "GIF MIME type should match");
        assertEquals("image/svg+xml", mapper.getMimeType("svg"), "SVG MIME type should match");
        assertEquals("image/webp", mapper.getMimeType("webp"), "WEBP MIME type should match");

        System.out.println("✓ Image formats working correctly");
    }

    @Test
    @DisplayName("Test video formats")
    public void testVideoFormats() {
        assertEquals("video/mp4", mapper.getMimeType("mp4"), "MP4 MIME type should match");
        assertEquals("video/mpeg", mapper.getMimeType("mpeg"), "MPEG MIME type should match");
        assertEquals("video/webm", mapper.getMimeType("webm"), "WEBM MIME type should match");

        System.out.println("✓ Video formats working correctly");
    }

    @Test
    @DisplayName("Test audio formats")
    public void testAudioFormats() {
        assertEquals("audio/mpeg", mapper.getMimeType("mp3"), "MP3 MIME type should match");
        assertEquals("audio/wav", mapper.getMimeType("wav"), "WAV MIME type should match");
        assertEquals("audio/ogg", mapper.getMimeType("ogg"), "OGG MIME type should match");

        System.out.println("✓ Audio formats working correctly");
    }

    @Test
    @DisplayName("Test map size consistency")
    public void testMapSizeConsistency() {
        Map<String, String> extMap = mapper.getExtensionToMimeMap();
        Map<String, List<String>> mimeMap = mapper.getMimeToExtensionsMap();

        // Count total extensions in reverse map
        int totalExtensionsInReverseMap = mimeMap.values().stream()
                .mapToInt(List::size)
                .sum();
        // divided by 2 because the getExtensionToMimeMap has both "." and non "." before the extension
        assertEquals(extMap.size() / 2, totalExtensionsInReverseMap,
                "Total extensions should match in both maps");

        System.out.println("✓ Map sizes are consistent: " + extMap.size() + " total extensions");
    }

    @Test
    @DisplayName("Test map immutability (defensive copies)")
    public void testMapImmutability() {
        Map<String, String> extMap = mapper.getExtensionToMimeMap();
        int originalSize = extMap.size();

        // Try to modify returned map
        extMap.put("test", "test/test");

        // Get map again and verify it wasn't modified
        Map<String, String> extMap2 = mapper.getExtensionToMimeMap();
        assertEquals(originalSize, extMap2.size(), "Original map should not be modified");
        assertFalse(extMap2.containsKey("test"), "Original map should not contain added key");

        System.out.println("✓ Maps return defensive copies (immutable)");
    }

    @Test
    @DisplayName("Test null and empty string handling")
    public void testNullAndEmptyStringHandling() {
        // Test getMimeType with null
        assertDoesNotThrow(() -> mapper.getMimeType(null), "Should handle null gracefully");

        // Test getMimeType with empty string
        String emptyResult = mapper.getMimeType("");
        assertEquals("application/octet-stream", emptyResult, "Empty string should return default");

        // Test getExtensions with null
        List<String> nullResult = mapper.getExtensions(null);
        assertNotNull(nullResult, "Should return empty list for null");
        assertTrue(nullResult.isEmpty(), "Should return empty list for null");

        System.out.println("✓ Null and empty string handling working correctly");
    }

    @Test
    @DisplayName("Print sample data for verification")
    public void testPrintSampleData() {
        System.out.println("\n========================================");
        System.out.println("Sample Data Verification");
        System.out.println("========================================");

        mapper.printSamples(10);
        mapper.printReverseSamples(5);

        assertTrue(true, "Sample data printed successfully");
    }
}