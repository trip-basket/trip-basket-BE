package dev.jino.tripbasketnew.oauth;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuthAttributes {

    private Map<String, Object> attributes; // 원본 attribute (추후 확장 및 디버깅에 사용)
    private String nameAttributeKey; // 사용자 식별 key 이름(sub, id 등)
    private String provider; // google, kakao, naver
    private String providerId; // 사용자 식별 id (nameAttributeKey의 값)
    private String email;
    private String name;

    public static OAuthAttributes of(
        String registrationId,
        String userNameAttributeName, // spring이 ClientRegistration에서 읽어온 사용자 식별 key 이름
        Map<String, Object> attributes
    ) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> ofGoogle(userNameAttributeName, attributes);
            default ->
                throw new IllegalArgumentException("Unsupported provider: " + registrationId);
        };
    }

    private static OAuthAttributes ofGoogle(String key, Map<String, Object> a) {
        return OAuthAttributes.builder()
            .nameAttributeKey(key) // google은 sub를 사용
            .provider("google")
            .providerId((String) a.get("sub"))
            .email((String) a.get("email"))
            .name((String) a.getOrDefault("name", a.get("email"))) // 이름이 없으면 email을 이름으로
            .attributes(a)
            .build();
    }
}
