package org.bookyourshows.dto;

public class Views {
    public interface Public {
    }

    public interface TheatreOwner extends Public {
    }

    public interface Admin extends TheatreOwner {
    }

    public static Class<?> resolveView(String role) {
        return switch (role.toUpperCase()) {
            case "ADMIN" -> Views.Admin.class;
            case "THEATRE_OWNER" -> TheatreOwner.class;
            default -> Views.Public.class;
        };
    }
}