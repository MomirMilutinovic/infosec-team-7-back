package com.lunark.lunark.auth.controller;

import com.lunark.lunark.auth.dto.AccountDto;
import com.lunark.lunark.auth.dto.AccountSignUpDto;
import com.lunark.lunark.auth.dto.AccountUpdatePasswordDto;
import com.lunark.lunark.auth.model.Account;
import com.lunark.lunark.auth.model.AccountRole;
import com.lunark.lunark.auth.model.LdapAccount;
import com.lunark.lunark.auth.service.IAccountService;
import com.lunark.lunark.auth.service.ICertificateRequestService;
import com.lunark.lunark.mapper.AccountDtoMapper;
import com.lunark.lunark.mapper.PropertyDtoMapper;
import com.lunark.lunark.notifications.dto.NotificationSettingsDto;
import com.lunark.lunark.properties.dto.PropertyResponseDto;
import com.lunark.lunark.properties.service.IPropertyService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    @Autowired
    IAccountService accountService;

    @Autowired
    ICertificateRequestService certificateRequestService;

    @Autowired
    IPropertyService propertyService;

    @Autowired
    ModelMapper modelMapper;

    @GetMapping(path="/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountDto> getAccount(@PathVariable("id") UUID id) {
        Optional<Account> account = accountService.find(id);
        if (account.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(modelMapper.map(account.get(), AccountDto.class), HttpStatus.OK);
    }

    @GetMapping(value ="/average/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Double> getAverageGrade(@PathVariable("id") UUID id) {
        Double averageGrade = accountService.getAverageGrade(id);
        if (averageGrade == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(averageGrade, HttpStatus.OK);
    }

    // Seems to be unused
    @GetMapping(path="/nonadmins", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AccountDto>> getNonAdmins(SpringDataWebProperties.Pageable pageable) {
        List<Account> nonAdmins = accountService.findAll().stream().filter(account -> account.getRole() != AccountRole.ADMIN).collect(Collectors.toList());
        List<AccountDto> accountDtos = nonAdmins.stream().map(AccountDtoMapper::fromAccountToDTO).collect(Collectors.toList());
        return new ResponseEntity<>(accountDtos, HttpStatus.OK);
    }

    @DeleteMapping(path="/{id}")
    @PreAuthorize("hasAuthority('delete_own_account')")
    public ResponseEntity<AccountDto> deleteOwnAccount(@PathVariable("id") UUID id) {
        Account currentUser = ((LdapAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).toAccount();
        if (!currentUser.getId().equals(id)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if(accountService.find(id).isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if(accountService.delete(id)) {
            return  new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }



    @PutMapping(path="/{id}")
    @PreAuthorize("hasAuthority('update_account')")
    public ResponseEntity<AccountDto> updateAccount(@RequestBody AccountSignUpDto accountDto, @PathVariable("id") UUID id) {
        Optional<Account> accountOptional = accountService.find(id);
        if(accountOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Account account = accountDto.toAccount();
        account.setId(id);
        accountService.update(account);
        return new ResponseEntity<>(modelMapper.map(account, AccountDto.class), HttpStatus.OK);
    }

    @PostMapping(value = "/create-certificate")
    @PreAuthorize("hasAuthority('create_certificate_request')")
    public ResponseEntity createCertificationRequest(@RequestBody AccountSignUpDto accountDto) {
        Optional<Account> accountOptional = accountService.find(accountDto.getId());
        if(accountOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Account account = accountDto.toAccount();
        certificateRequestService.create(account);
        return new ResponseEntity<>(null, HttpStatus.CREATED);
    }



    @PutMapping("/update-password")
    @PreAuthorize("hasAuthority('update_account')")
    public ResponseEntity<AccountDto> updatePassword(@RequestBody AccountUpdatePasswordDto passwordUpdateDto) {
        boolean isUpdated = accountService.updatePassword(
                passwordUpdateDto.getAccountId(),
                passwordUpdateDto.getOldPassword(),
                passwordUpdateDto.getNewPassword());
        if (isUpdated) { return new ResponseEntity<>(HttpStatus.OK); }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping(path="/favorites/{id}")
    @PreAuthorize("hasAuthority('write_favorites')")
    public ResponseEntity<?> addPropertyToFavorites(@PathVariable("id") Long propertyId) {
        Account currentUser = ((LdapAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).toAccount();
        accountService.addToFavorites(currentUser.getId(), propertyService.find(propertyId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Property not found")));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(path="/favorites/{id}")
    @PreAuthorize("hasAuthority('write_favorites')")
    public ResponseEntity<?> removePropertyFromFavorites(@PathVariable("id") Long propertyId) {
        Account currentUser = ((LdapAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).toAccount();
        accountService.removeFromFavorites(currentUser.getId(), propertyService.find(propertyId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Property not found")));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/favorites", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('write_favorites') or hasAuthority('read_favorites')")
    public ResponseEntity<List<PropertyResponseDto>> getFavoriteProperties() {
        Account currentUser = ((LdapAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).toAccount();
        List<PropertyResponseDto> favoriteProperties = accountService.getFavoriteProperties(currentUser.getId()).stream()
                .map(PropertyDtoMapper::fromPropertyToDto)
                .toList();

        return new ResponseEntity<>(favoriteProperties, HttpStatus.OK);
    }

    @PostMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('update_account')")
    public ResponseEntity<?> saveProfileImage(@RequestParam("image") MultipartFile file) {
        Account currentUser = ((LdapAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).toAccount();

        try {
            accountService.saveProfileImage(currentUser.getId(), file);
        } catch (IOException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/profile-image", produces = MediaType.IMAGE_JPEG_VALUE)
    @PreAuthorize("hasAuthority('update_account')")
    public ResponseEntity<byte[]> getProfileImage() {
        LdapAccount currentUser = (LdapAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Account account = accountService.find(currentUser.getUuid()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account not found"));

        byte[] profileImage = account.getProfileImage().getImageData();

        return new ResponseEntity<>(profileImage, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}/profile-image", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getProfileImage(@PathVariable("id") UUID userId) {
        Optional<Account> account = this.accountService.find(userId);
        if (account.isEmpty() || account.get().getProfileImage() == null) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        byte[] profileImage = account.get().getProfileImage().getImageData();
        return new ResponseEntity<>(profileImage, HttpStatus.OK);
    }

    @PutMapping(value = "notifications", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('read_notifications')")
    public ResponseEntity<AccountDto> toggleNotifications(@RequestBody NotificationSettingsDto dto) {
        Account account = ((LdapAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).toAccount();
        Account updatedAccount = this.accountService.toggleNotifications(account.getId(), dto.getType());

        AccountDto response = AccountDtoMapper.fromAccountToDTO(updatedAccount);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }




}
