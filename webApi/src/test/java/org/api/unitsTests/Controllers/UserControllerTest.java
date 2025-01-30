package org.api.unitsTests.Controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.api.controllers.user.UserController;
import org.api.controllers.user.UserExceptions;
import org.api.internal.StubCompanyRepo;
import org.api.internal.StubUserRepo;
import org.api.requests.UserRegisterRequest;
import org.common.RoleManager;
import org.data.entities.AppUser;
import org.data.entities.Company;
import org.data.entities.CompanyWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;
import org.utils.CustomGenerator;

import java.lang.reflect.Field;
import java.util.Map;

public class UserControllerTest {
    private final StubCompanyRepo companyRepo;
    private final StubUserRepo userRepo;
    private final UserController con;
    private final CustomGenerator gen;

    public UserControllerTest() throws NoSuchFieldException, IllegalAccessException {
        this.companyRepo = new StubCompanyRepo();
        this.userRepo = new StubUserRepo(this.companyRepo);
        this.gen = new CustomGenerator();
        this.con = new UserController(this.companyRepo, this.userRepo);
    }


    @BeforeEach
    void clearUsers() {
        this.userRepo.deleteAll();
    }

    @Test
    void testExistingUserName() {
        // add the owner users
        this.userRepo.addAdmins();

        for (AppUser u: this.userRepo.getDb()) {
            CompanyWrapper w = new CompanyWrapper(u.getCompany());

            UserRegisterRequest req = new UserRegisterRequest(
                    w.getId(),
                    u.getUsername(),
                    u.getPassword(),
                    u.getRole().toString(),
                    "incorrect_token"
                    );

            // throw an error related to username: used username
            Assertions.assertThrows( UserExceptions.AlreadyExistingUserException.class,
                    () -> this.con.registerUser(req));
        }

        this.userRepo.deleteAll();
    }


    @Test
    void testNonExistingCompany() {
        // add the owner users
        this.userRepo.addAdmins();

        for (AppUser u: this.userRepo.getDb()) {
            UserRegisterRequest req = new UserRegisterRequest(
                    this.gen.randomString(100), // the company id is random here and is not saved in the database
                    u.getUsername(),
                    u.getPassword(),
                    u.getRole().toString(),
                    "incorrect_token"
            );

            // throw an error related to username: used username
            Assertions.assertThrows( UserExceptions.UserWithNoCompanyException.class,
                    () -> this.con.registerUser(req));
        }

        this.userRepo.deleteAll();
    }

    @Test
    void testNoExistingRole() {
        // add the owner users
        this.userRepo.addAdmins();

        for (AppUser u: this.userRepo.getDb()) {
            CompanyWrapper w = new CompanyWrapper(u.getCompany());

            UserRegisterRequest req = new UserRegisterRequest(
                    w.getId(),
                    "new_user_name",
                    "new_password_1",
                    "no_existing_role",
                    "incorrect_token"
            );

            // throw an error related to username: used username
            Assertions.assertThrows( RoleManager.NoExistingRoleException.class,
                    () -> this.con.registerUser(req));
        }

        this.userRepo.deleteAll();
    }

    @Test
    void testIncorrectRoleToken() {
        // add the owner users
        this.userRepo.addAdmins();

        CompanyWrapper c = new CompanyWrapper(this.userRepo.getDb().getFirst().getCompany());

        UserRegisterRequest req = new UserRegisterRequest(
                c.getId(),
                "new_username_1",
                "new_password_1",
                "owner",
                "incorrect_token"
        );

        // throw an error related to username: used username
        Assertions.assertThrows(UserExceptions.IncorrectRoleTokenException.class,
                () -> this.con.registerUser(req));

        this.userRepo.deleteAll();
    }

    @Test
    void testRegisterValidUser() throws NoSuchFieldException, IllegalAccessException, JsonProcessingException {
        Assertions.assertEquals(0, this.userRepo.count());

        Company c = this.companyRepo.getDb().getFirst();
        Field f = c.getClass().getDeclaredField("roleTokens");
        f.setAccessible(true);
        Map<String, String> tokens = (Map<String, String>) f.get(c);

        Field f1 = c.getClass().getDeclaredField("id");
        f1.setAccessible(true);
        String id = (String) f1.get(c);

        UserRegisterRequest req = new UserRegisterRequest(
                id,
                "username",
                "password",
                RoleManager.getRole("owner").toString(),
                tokens.get("owner"));

        UserRegisterRequest finalReq1 = req;
        Assertions.assertDoesNotThrow(() -> this.con.registerUser(finalReq1));

        Assertions.assertEquals(1, this.userRepo.count());

        this.userRepo.deleteAll();

        Assertions.assertEquals(0, this.userRepo.count());

        req = new UserRegisterRequest(
                id,
                "username",
                "password",
                RoleManager.getRole("admin").toString(),
                tokens.get("admin"));

        // trying to add a non-owner use to a company with no owner should raise an error
        UserRegisterRequest finalReq = req;
        Assertions.assertThrows(UserExceptions.UserBeforeOwnerException.class, ()  -> this.con.registerUser(finalReq));
    }

    @Test
    void testNoMultipleOwners() throws NoSuchFieldException, IllegalAccessException {
        this.userRepo.addOwners();
        Assertions.assertEquals(2, this.userRepo.count());

        Company c = this.companyRepo.getDb().getFirst();
        Field f = c.getClass().getDeclaredField("roleTokens");
        f.setAccessible(true);
        Map<String, String> tokens = (Map<String, String>) f.get(c);

        Field f1 = c.getClass().getDeclaredField("id");
        f1.setAccessible(true);
        String id = (String) f1.get(c);

        UserRegisterRequest req = new UserRegisterRequest(
                id,
                "username",
                "password",
                RoleManager.getRole("owner").toString(),
                tokens.get("owner"));

        Assertions.assertThrows(UserExceptions.MultipleOwnersException.class,
                () -> this.con.registerUser(req));

        this.userRepo.deleteAll();
    }


}
