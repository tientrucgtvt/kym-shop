package com.kinshop.web;

import com.kinshop.security.AccessRight;
import com.kinshop.security.AccessRightRepository;
import com.kinshop.security.AppPage;
import com.kinshop.security.AppUser;
import com.kinshop.security.AppUserRepository;
import com.kinshop.security.PermissionService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/access-rights")
public class AccessRightController {

    private final AppUserRepository appUserRepository;
    private final AccessRightRepository accessRightRepository;
    private final PasswordEncoder passwordEncoder;
    private final PermissionService permissionService;

    public AccessRightController(
            AppUserRepository appUserRepository,
            AccessRightRepository accessRightRepository,
            PasswordEncoder passwordEncoder,
            PermissionService permissionService
    ) {
        this.appUserRepository = appUserRepository;
        this.accessRightRepository = accessRightRepository;
        this.passwordEncoder = passwordEncoder;
        this.permissionService = permissionService;
    }

    @GetMapping
    public String index(@RequestParam(required = false) Long userId, Model model) {
        permissionService.require("ACCESS_RIGHT", "VIEW");
        List<AppUser> users = appUserRepository.findAllByOrderByUsernameAsc();
        AppUser selectedUser = userId == null
                ? users.stream().findFirst().orElse(null)
                : appUserRepository.findById(userId).orElse(null);
        Map<String, AccessRight> rights = selectedUser == null
                ? Map.of()
                : accessRightRepository.findByAppUserOrderByPageKeyAsc(selectedUser).stream()
                .collect(Collectors.toMap(AccessRight::getPageKey, right -> right));
        model.addAttribute("users", users);
        model.addAttribute("selectedUser", selectedUser);
        model.addAttribute("pages", AppPage.all());
        model.addAttribute("rights", rights);
        return "access-rights/index";
    }

    @PostMapping("/users")
    public String createUser(
            String username,
            String fullName,
            String password,
            RedirectAttributes redirectAttributes
    ) {
        permissionService.require("ACCESS_RIGHT", "CREATE");
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Username and password are required.");
            return "redirect:/access-rights";
        }
        AppUser user = new AppUser();
        user.setUsername(username.trim());
        user.setFullName(fullName);
        user.setPassword(passwordEncoder.encode(password));
        user.setActive(true);
        appUserRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "User created.");
        return "redirect:/access-rights?userId=" + user.getId();
    }

    @PostMapping("/save")
    public String saveRights(
            Long userId,
            @RequestParam Map<String, String> params,
            RedirectAttributes redirectAttributes
    ) {
        permissionService.require("ACCESS_RIGHT", "UPDATE");
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setActive(params.containsKey("active"));
        appUserRepository.save(user);

        for (AppPage page : AppPage.values()) {
            AccessRight right = accessRightRepository.findByAppUserAndPageKey(user, page.name()).orElseGet(() -> {
                AccessRight newRight = new AccessRight();
                newRight.setAppUser(user);
                newRight.setPageKey(page.name());
                return newRight;
            });
            String prefix = page.name() + "_";
            right.setCanView(params.containsKey(prefix + "VIEW"));
            right.setCanCreate(params.containsKey(prefix + "CREATE"));
            right.setCanUpdate(params.containsKey(prefix + "UPDATE"));
            right.setCanDelete(params.containsKey(prefix + "DELETE"));
            right.setCanCancel(params.containsKey(prefix + "CANCEL"));
            right.setCanReturn(params.containsKey(prefix + "RETURN"));
            right.setCanStock(params.containsKey(prefix + "STOCK"));
            accessRightRepository.save(right);
        }
        redirectAttributes.addFlashAttribute("success", "Access rights saved.");
        return "redirect:/access-rights?userId=" + userId;
    }
}
