package dev.andreasgeorgatos.pointofservicebackend.security;

import dev.andreasgeorgatos.pointofservicebackend.models.users.Permission;
import dev.andreasgeorgatos.pointofservicebackend.models.users.Role;
import dev.andreasgeorgatos.pointofservicebackend.models.users.Users;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class UserPrincipal implements UserDetails {

    private final Users users;

    public UserPrincipal(Users users) {
        this.users = users;
    }


    public Long getId() {
        return users.getId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();

        if (users.getRoles() == null) {
            return authorities;
        }

        for (Role role : users.getRoles()) {
            authorities.add(new SimpleGrantedAuthority(role.getName()));

            if (role.getPermissions() == null) {
                continue;
            }

            for (Permission permission : role.getPermissions()) {
                authorities.add(new SimpleGrantedAuthority(permission.getName()));
            }
        }

        return authorities;
    }

    @Override
    public String getPassword() {
        return users.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return users.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return users.isEnabled();
    }

}
