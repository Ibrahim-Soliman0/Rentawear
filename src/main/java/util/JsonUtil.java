package util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class JsonUtil {

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls() // null fields would still appear in json as null
            .create();

    private JsonUtil() {}

    /// used by servlet
    public static void writeJson(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        resp.getWriter().write(GSON.toJson(data));
    }

    /// used by JSP
    public static String toJson(Object data) {
        return GSON.toJson(data);
    }
}