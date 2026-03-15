package util;

import net.coobird.thumbnailator.Thumbnails;
import java.io.*;

public final class ImageProcessor {

    private static final int[]    SIZE_PX    = {400, 800, 1400};
    private static final String[] SIZE_NAMES = {"sm", "md", "lg"};
    private static final float    QUALITY    = 0.85f;

    private ImageProcessor() {}

    public static void process(InputStream inputStream, File targetDir, String uuid)
            throws IOException {

        targetDir.mkdirs();

        File temp = File.createTempFile("rw_", ".tmp");
        try {
            try (OutputStream out = new FileOutputStream(temp)) {
                inputStream.transferTo(out);
            }

            for (int i = 0; i < SIZE_PX.length; i++) {
                File jpg = new File(targetDir, uuid + "_" + SIZE_NAMES[i] + ".jpg");

                Thumbnails.of(temp)
                        .width(SIZE_PX[i])
                        .keepAspectRatio(true)
                        .outputFormat("jpg")
                        .outputQuality(QUALITY)
                        .toFile(jpg);
            }
        } finally {
            temp.delete();
        }
    }

    public static void deleteAll(String basePath, String webappRoot) {
        for (String size : SIZE_NAMES)
            new File(webappRoot + basePath + "_" + size + ".jpg").delete();
    }
}