package net.zjvis.flint.data.hub.entity.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.extern.jackson.Jacksonized;
import net.zjvis.flint.data.hub.security.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@EqualsAndHashCode
@ToString
@Entity
public class UniversalUser {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ElementCollection
    @CollectionTable(name = "userRoleList", joinColumns = @JoinColumn(name = "id"))
    @Column(nullable = false)
    @Singular("role")
    private List<Role> roleList;

    private Date expireDate;

    @Builder.Default
    private boolean locked = false;

    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roleList.stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public boolean isAccountNonExpired() {
        return null == expireDate || System.currentTimeMillis() < expireDate.getTime();
    }

    @JsonIgnore
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @JsonIgnore
    public boolean isEnabled() {
        return isAccountNonLocked() && isAccountNonExpired();
    }

    public String toJson() throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(this);
    }
}