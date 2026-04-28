package backendlab.team4you.registryaccess;

import backendlab.team4you.registry.Registry;
import backendlab.team4you.user.UserEntity;
import jakarta.persistence.*;

@Entity
@Table(
        name = "registry_access",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"registry_id", "user_id"}
                )
        }
)
public class RegistryAccessEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registry_id", nullable = false)
    private Registry registry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "can_create_cases", nullable = false)
    private boolean canCreateCases;

    public RegistryAccessEntity() {
    }

    public RegistryAccessEntity(
            Registry registry,
            UserEntity user,
            boolean canCreateCases
    ) {
        this.registry = registry;
        this.user = user;
        this.canCreateCases = canCreateCases;
    }

    public Long getId() {
        return id;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public boolean isCanCreateCases() {
        return canCreateCases;
    }

    public void setCanCreateCases(boolean canCreateCases) {
        this.canCreateCases = canCreateCases;
    }
}