package service;

import db.UserDatabase;
import model.User;
import util.error.HttpsErrorMessage;
import util.error.erroclass.FailLoggedException;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

public class UserService {

    private final String[] userKey = {"userId", "password", "name", "email"};

    private UserDatabase userDatabase = new UserDatabase();


    public User createModel(Map<String, String> userMap) {
        return new User(URLDecoder.decode(userMap.get(userKey[0]), StandardCharsets.UTF_8)
                , URLDecoder.decode(userMap.get(userKey[1]), StandardCharsets.UTF_8),
                URLDecoder.decode(userMap.get(userKey[2]), StandardCharsets.UTF_8),
                URLDecoder.decode(userMap.get(userKey[3]), StandardCharsets.UTF_8));
    }

    public User validLogin(Map<String, String> loginInfo) throws FailLoggedException {
        String loginUserId = loginInfo.get(userKey[0]);
        String loginPassword = loginInfo.get(userKey[1]);

        Optional<User> optionalUser = userDatabase.findObjectById(loginUserId);
        if (optionalUser.isEmpty()) {
            throw new FailLoggedException(HttpsErrorMessage.NOT_LOGGED);
        }
        User user = optionalUser.get();
        if (!isMatchLogin(loginUserId, loginPassword, user)) {
            throw new FailLoggedException(HttpsErrorMessage.NOT_LOGGED);
        }
        return user;
    }

    private static boolean isMatchLogin(String loginUserId, String loginPassword, User user) {
        return user.getUserId().equals(loginUserId) && user.getPassword().equals(loginPassword);
    }

}
