package com.lunark.lunark.auth.model;

import com.lunark.lunark.notifications.model.GuestNotificationSettings;
import com.lunark.lunark.notifications.model.HostNotificationSettings;
import lombok.Data;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;
import java.util.UUID;

@Entry(
        objectClasses = {
                "bookingUser"
        },
        base = "dc=booking,ou=users,ou=system"
)
@Data
public class LdapAccount {

    @Id
    private Name dn;

    @Attribute(name = "id")
    private UUID id;

    @Attribute(name = "email")
    private String email;

    @Attribute(name = "name")
    private String name;

    @Attribute(name = "sn")
    private String surname;

    @Attribute(name = "address")
    private String address;

    @Attribute(name = "phoneNumber")
    private String phoneNumber;

    @Attribute(name = "blocked")
    private boolean blocked;

    @Attribute(name = "deleted")
    private boolean deleted;

    // TODO: Check if role is needed in LDAP entity

    @Attribute(name = "notifyOnHostReview")
    private boolean notifyOnHostReview;

    @Attribute(name = "notifyOnPropertyReview")
    private boolean notifyOnPropertyReview;

    @Attribute(name = "notifyOnReservationCancellation")
    private boolean notifyOnReservationCancellation;

    @Attribute(name = "notifyOnReservationCreation")
    private boolean notifyOnReservationCreation;

    @Attribute(name = "notifyOnReservationRequestResponse")
    private boolean notifyOnReservationRequestResponse;

    @Attribute(name = "mimeType")
    private String profileImageMimeType;

    @Attribute(name = "imageData")
    private byte[] profileImageData;


    public LdapAccount() {
    }

    public GuestNotificationSettings getGuestNotificationSettings() {
        return new GuestNotificationSettings(notifyOnReservationRequestResponse);
    }

    public HostNotificationSettings getHostNotificationSettings() {
        return new HostNotificationSettings(notifyOnReservationCreation, notifyOnReservationCancellation, notifyOnHostReview, notifyOnPropertyReview);
    }

    public Account toAccount() {
        Account account = new Account(
                id,
                email,
                name,
                surname,
                address,
                phoneNumber,
                AccountRole.GUEST,
                null,
                null
        );
        account.setGuestNotificationSettings(this.getGuestNotificationSettings());
        account.setHostNotificationSettings(this.getHostNotificationSettings());
        account.setDeleted(false);
        if (profileImageData != null) {
            account.setProfileImage(new ProfileImage(profileImageData, profileImageMimeType));
        }
        return account;
    }

    public void copyFields(Account account) {
        this.setEmail(account.getEmail());
        this.setName(account.getName());
        this.setSurname(account.getSurname());
        this.setAddress(account.getAddress());
        if (account.getProfileImage() != null) {
            this.setProfileImageData(account.getProfileImage().getImageData());
            this.setProfileImageMimeType(account.getProfileImage().getMimeType());
        }
    }
}
