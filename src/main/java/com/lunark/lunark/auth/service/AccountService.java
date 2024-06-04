package com.lunark.lunark.auth.service;

import com.lunark.lunark.auth.model.Account;
import com.lunark.lunark.auth.model.AccountRole;
import com.lunark.lunark.auth.model.LdapAccount;
import com.lunark.lunark.auth.model.ProfileImage;
import com.lunark.lunark.auth.repository.IAccountRepository;
import com.lunark.lunark.auth.repository.ILdapAccountRepository;
import com.lunark.lunark.notifications.model.NotificationType;
import com.lunark.lunark.properties.model.Property;
import com.lunark.lunark.properties.service.IPropertyService;
import com.lunark.lunark.reservations.model.Reservation;
import com.lunark.lunark.reservations.model.ReservationStatus;
import com.lunark.lunark.reservations.service.IReservationService;
import com.lunark.lunark.reviews.model.Review;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.*;

@Service
public class AccountService implements IAccountService {
    @Autowired
    IAccountRepository accountRepository;

    @Autowired
    IReservationService reservationService;

    @Autowired
    IPropertyService propertyService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ILdapAccountRepository ldapAccountRepository;


    @Override
    public Collection<Account> findAll() {
        return accountRepository.findAll();
    }


    @Override
    public Account create(Account account) {
        return accountRepository.saveAndFlush(account);
    }

    @Override
    public Optional<Account> find(UUID id) {
        return accountRepository.findById(id);
    }

    @Override
    public Optional<Account> find(String email) {
        return accountRepository.findByEmail(email);
    }

    @Override
    public Account updateSql(Account account) {
        // TODO: Update in LDAP
        Optional<Account> oldAccountOptional = accountRepository.findById(account.getId());
        if (oldAccountOptional.isEmpty()) {
            return null;
        }
        Account oldAccount = oldAccountOptional.get();
        if (oldAccount.getProfileImage() != null) {
            account.setProfileImage(oldAccount.getProfileImage());
        }
        if (account.getHostNotificationSettings() == null && account.getGuestNotificationSettings() == null) {
            account.setGuestNotificationSettings(oldAccount.getGuestNotificationSettings());
            account.setHostNotificationSettings(oldAccount.getHostNotificationSettings());
        }
        return accountRepository.saveAndFlush(account);
    }

    public boolean updateLdap(Account account) {
        Optional<LdapAccount> ldapAccountOptional = ldapAccountRepository.findByEmail(account.getEmail());
        if (ldapAccountOptional.isEmpty()) {
            return false;
        }
        LdapAccount ldapAccount = ldapAccountOptional.get();
        ldapAccount.copyFields(account);
        ldapAccountRepository.save(ldapAccount);
        return true;
    }

    @Override
    public boolean delete(UUID id) {
        // TODO: Delete account in LDAP
        Optional<Account> accountToRemove = accountRepository.findById(id);
        if (accountToRemove.isEmpty()) {
            return false;
        }
        Account account = accountToRemove.get();
        AccountRole accountRole = account.getRole();

        if (isGuestAccount(accountRole)) {
            handleUserAccountDeletion(id);
        } else {
            List<Property> propertiesList = propertyService.findAllPropertiesForHost(account.getId());
            handleHostAccountDeletion(id, propertiesList);
        }
        accountRepository.deleteById(id);
        return true;
    }

    private boolean isGuestAccount(AccountRole accountRole) {
        return accountRole == AccountRole.GUEST;
    }

    private void handleUserAccountDeletion(UUID userId) {
        List<Reservation> reservationList = reservationService.getAllReservationsForUser(userId);
        if (noAcceptedReservations(reservationList)) {
            accountRepository.deleteById(userId);
        }
    }

    private void handleHostAccountDeletion(UUID hostId, List<Property> propertiesList) {
        List<Reservation> reservationList = reservationService.getAllReservationsForPropertiesList(propertiesList);
        if (noAcceptedReservations(reservationList)) {
            for (Property property : propertiesList) {
                propertyService.delete(property.getId());
            }
            accountRepository.deleteById(hostId);
        }
    }

    public static boolean noAcceptedReservations(List<Reservation> reservationList) {
        return reservationList == null || reservationList.stream().noneMatch(reservation -> reservation.getStatus() == ReservationStatus.ACCEPTED);
    }

    @Override
    public void cancelAllReservations(List<Reservation> reservationList) {
        for(Reservation reservation: reservationList) {
            if(reservation.getStatus() != ReservationStatus.CANCELLED) {
                reservation.setStatus(ReservationStatus.CANCELLED);
                reservationService.saveOrUpdate(reservation);
            }
        }
    }

    @Override
    public Account toggleNotifications(UUID accountId, NotificationType type) {
        Optional<Account> account = find(accountId);

        return account.map(a -> {
            a.toggleNotifications(type);
            return accountRepository.save(a);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account with this id does not exist."));
    }

    @Override
    public boolean updatePassword(UUID accountId, String oldPassword, String newPassword) {
        // TODO: Update password in LDAP
        Optional<Account> accountToUpdate = accountRepository.findById(accountId);
        if (accountToUpdate.isEmpty() || !isOldPasswordCorrect(accountToUpdate.get(), oldPassword)) {
            return false;
        }
        updateAccountPassword(accountToUpdate.get(), newPassword);
        return true;
    }

    private boolean isOldPasswordCorrect(Account account, String oldPassword) {
        // TODO: Check password from LDAP
        String currentPassword = account.getPassword();
        return passwordEncoder.matches(oldPassword, currentPassword);
    }

    private void updateAccountPassword(Account account, String newPassword) {
        // TODO: Encode password for LDAP
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        accountRepository.saveAndFlush(account);
    }

    @Override
    public Double getAverageGrade(UUID id) {
        Optional<Account> account = this.find(id);
        if (account.isEmpty()) {
            return null;
        }
        Account foundAccount = account.get();
        return calculateAverageGrade(foundAccount);
    }

    @Override
    public Collection<Property> getFavoriteProperties(UUID accountId) {
        Optional<Account> account = this.find(accountId);
        if (account.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account not found");
        }

        Hibernate.initialize(account.get().getFavoriteProperties());
        return account.get().getFavoriteProperties();
    }

    @Override
    public void removeFromFavorites(UUID id, Property property) {
        this.find(id).ifPresentOrElse(account -> {
            account.getFavoriteProperties().remove(property);
            updateSql(account);
        }, () -> {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account not found");
        });
    }

    @Override
    public void saveProfileImage(UUID accountId, MultipartFile file) throws IOException {
        Optional<Account> account = this.find(accountId);
        if (account.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account not found");
        }

        byte[] byteObjects = new byte[file.getBytes().length];

        int i = 0;

        for (byte b : file.getBytes()) {
            byteObjects[i++] = b;
        }

        ProfileImage profileImage = new ProfileImage();
        profileImage.setImageData(byteObjects);
        profileImage.setMimeType(file.getContentType());

        account.get().setProfileImage(profileImage);

        accountRepository.save(account.get());
        updateLdap(account.get());
    }

    @Override
    public void saveAndFlush(Account account) {
        accountRepository.saveAndFlush(account);
    }

    private Double calculateAverageGrade(Account account) {
        ArrayList<Review> reviewList = (ArrayList<Review>) account.getReviews();
        if (reviewList.isEmpty()) {
            return 0.0;
        }
        double sum = reviewList.stream().mapToDouble(Review::getRating).sum();
        return sum / reviewList.size();
    }

    @Override
    public void addToFavorites(UUID id, Property property) {
        this.find(id).ifPresentOrElse(account -> {
            account.getFavoriteProperties().add(property);
            updateSql(account);
        }, () -> {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account not found");
        });
    }
}
