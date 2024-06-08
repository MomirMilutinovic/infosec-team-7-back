package com.lunark.lunark.reservations.controller;

import com.lunark.lunark.auth.model.Account;
import com.lunark.lunark.auth.model.AccountRole;
import com.lunark.lunark.auth.model.LdapAccount;
import com.lunark.lunark.mapper.ReservationDtoMapper;
import com.lunark.lunark.reservations.dto.ReservationResponseDto;
import com.lunark.lunark.reservations.dto.ReservationDto;
import com.lunark.lunark.reservations.dto.ReservationRequestDto;
import com.lunark.lunark.reservations.dto.ReservationSearchDto;
import com.lunark.lunark.reservations.model.Reservation;
import com.lunark.lunark.reservations.model.ReservationStatus;
import com.lunark.lunark.reservations.service.IReservationService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final IReservationService reservationService;
    private final ModelMapper modelMapper;

    @Autowired
    public ReservationController(IReservationService reservationService, ModelMapper modelMapper) {
        this.reservationService = reservationService;
        this.modelMapper = modelMapper;
    }

    @PostMapping(path = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('write_reservation')")
    public ResponseEntity<ReservationResponseDto> createReservation(@Valid @RequestBody ReservationRequestDto dto) {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Reservation reservation = this.reservationService.create(dto, user.getUsername());
        ReservationResponseDto response = modelMapper.map(reservation, ReservationResponseDto.class);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping(path = "/{id}")
    @PreAuthorize("hasAuthority('write_reservation')")
    public ResponseEntity<ReservationDto> deleteReservation(@PathVariable("id") Long id) {
        Account account = ((LdapAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).toAccount();
        reservationService.deleteReservation(id, account.getId());

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping(path = "/accept/{reservation_id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('reply_reservation')")
    public ResponseEntity<ReservationDto> acceptReservation(@PathVariable("reservation_id") Long id) {
        Optional<Reservation>  reservationOptional = reservationService.findById(id);
        if(reservationOptional.isPresent()) {
            Account currentUser = ((LdapAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).toAccount();
            if (!currentUser.getId().equals(reservationOptional.get().getProperty().getHost().getId())) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            Reservation reservation = reservationOptional.get();
            reservationService.acceptOrRejectReservation(reservation, ReservationStatus.ACCEPTED);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(path = "/reject/{reservation_id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('reply_reservation')")
    public ResponseEntity<ReservationDto> rejectReservation(@PathVariable("reservation_id") Long id) {
        Optional<Reservation>  reservationOptional = reservationService.findById(id);
        if(reservationOptional.isPresent()) {
            Account currentUser = ((LdapAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).toAccount();
            if (!currentUser.getId().equals(reservationOptional.get().getProperty().getHost().getId())) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
            Reservation reservation = reservationOptional.get();
            reservationService.acceptOrRejectReservation(reservation, ReservationStatus.REJECTED);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping(path = "/cancel/{reservation_id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('write_reservation')")
    public ResponseEntity<ReservationDto> cancelReservation(@PathVariable("reservation_id") Long id) {
        Optional<Reservation> reservation = reservationService.findById(id);
        if (reservation.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Account currentUser = ((LdapAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).toAccount();
        if (!currentUser.getId().equals(reservation.get().getGuest().getId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if (reservationService.cancelReservation(reservation.get()) == false) {
            return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        }
        return ResponseEntity.ok(ReservationDtoMapper.fromReservationToDto(reservation.get()));
    }

    @GetMapping(value="/incoming-reservations", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('reply_reservation')")
    public ResponseEntity<List<ReservationDto>> getIncomingReservations(@RequestParam("hostId") UUID hostId) {
        Account currentUser = ((LdapAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).toAccount();
        if (!currentUser.getId().equals(hostId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        List<Reservation> reservations = reservationService.getIncomingReservationsForHostId(hostId).stream().filter(reservation -> ReservationStatus.PENDING.equals(reservation.getStatus())).toList();
        if(reservations.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<ReservationDto> reservationDtos = reservations.stream().map(ReservationDtoMapper::fromReservationToDto) .toList();
        return new ResponseEntity<>(reservationDtos, HttpStatus.OK);
    }

    @GetMapping(value="/accepted-reservations", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('write_reservation')")
    public ResponseEntity<List<ReservationDto>> getAcceptedReservations(@RequestParam("guestId") UUID guestId) {
        Account currentUser = ((LdapAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).toAccount();
        if (!currentUser.getId().equals(guestId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        List<Reservation> reservations = reservationService.getAllAcceptedReservations(guestId);
        if(reservations.isEmpty()) { return new ResponseEntity<>(HttpStatus.NOT_FOUND); }
        List<ReservationDto> reservationDtos = reservations.stream().map(ReservationDtoMapper::fromReservationToDto) .toList();
        return new ResponseEntity<>(reservationDtos, HttpStatus.OK);
    }
    @GetMapping(value = "/current")
    @PreAuthorize("hasAuthority('write_reservation') or hasAuthority('reply_reservation')")
    public ResponseEntity<List<ReservationResponseDto>> getReservationsForCurrentUser(
            @RequestParam(required = false) String propertyName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) ReservationStatus status
            ) {
        Account currentUser = ((LdapAccount) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).toAccount();
        boolean isHost = currentUser.getRole() == AccountRole.HOST;

        ReservationSearchDto dto = ReservationSearchDto.builder()
                .propertyName(propertyName)
                .startDate(startDate)
                .endDate(endDate)
                .status(status)
                .accountId(currentUser.getId())
                .build();

        List<ReservationResponseDto> reservations = reservationService.findByFilter(dto, isHost).stream()
                .map(reservation -> modelMapper.map(reservation, ReservationResponseDto.class))
                .toList();

        return new ResponseEntity<>(reservations, HttpStatus.OK);
    }

}
