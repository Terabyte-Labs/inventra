package mx.terabyte.labs.inventra.catalog.supplier;

import lombok.RequiredArgsConstructor;
import mx.terabyte.labs.inventra.catalog.supplier.dto.*;
import mx.terabyte.labs.inventra.common.exception.BusinessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository repository;
    private final SupplierContactRepository contactRepository;

    public Page<SupplierResponse> findAll(
            String name,
            Pageable pageable
    ) {

        Specification<SupplierEntity> spec = Specification.unrestricted();

        if (name != null && !name.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(
                            cb.lower(root.get("name")),
                            "%" + name.toLowerCase() + "%"
                    )
            );
        }

        return repository.findAll(spec, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public SupplierResponse create(CreateSupplierRequest request) {

        repository.findByCode(request.code())
                .ifPresent(existing -> {
                    throw new BusinessException(
                            "SUPPLIER_ALREADY_EXISTS",
                            "Supplier already exists with code: " + request.code()
                    );
                });

        SupplierEntity supplier = new SupplierEntity();

        supplier.setId(UUID.randomUUID());
        supplier.setCode(request.code());
        supplier.setName(request.name());
        supplier.setActive(true);
        supplier.setCreatedAt(LocalDateTime.now());

        SupplierEntity savedSupplier = repository.save(supplier);

        if (request.contacts() != null) {
            for (CreateSupplierContactRequest contactRequest : request.contacts()) {
                SupplierContactEntity contact = new SupplierContactEntity();

                contact.setId(UUID.randomUUID());
                contact.setSupplier(savedSupplier);
                contact.setName(contactRequest.name());
                contact.setEmail(contactRequest.email());
                contact.setPhone(contactRequest.phone());
                contact.setPosition(contactRequest.position());
                contact.setPrimaryContact(Boolean.TRUE.equals(contactRequest.primaryContact()));
                contact.setActive(true);
                contact.setCreatedAt(LocalDateTime.now());

                contactRepository.save(contact);
            }
        }

        return toResponse(savedSupplier);
    }

    private SupplierResponse toResponse(SupplierEntity entity) {
        List<SupplierContactResponse> contacts = contactRepository
                .findBySupplierId(entity.getId())
                .stream()
                .map(contact -> new SupplierContactResponse(
                        contact.getId(),
                        contact.getName(),
                        contact.getEmail(),
                        contact.getPhone(),
                        contact.getPosition(),
                        contact.getPrimaryContact(),
                        contact.getActive()
                ))
                .toList();

        return new SupplierResponse(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getActive(),
                contacts
        );
    }

    @Transactional(readOnly = true)
    public SupplierResponse findByCode(String code) {

        SupplierEntity supplier = repository.findByCode(code)
                .orElseThrow(() -> new BusinessException(
                        "SUPPLIER_NOT_FOUND",
                        "Supplier not found for code: " + code
                ));

        return toResponse(supplier);
    }

    @Transactional
    public SupplierResponse update(
            String code,
            UpdateSupplierRequest request
    ) {

        SupplierEntity supplier = repository.findByCode(code)
                .orElseThrow(() -> new BusinessException(
                        "SUPPLIER_NOT_FOUND",
                        "Supplier not found for code: " + code
                ));

        supplier.setName(request.name());
        supplier.setActive(request.active());

        repository.save(supplier);

        return toResponse(supplier);
    }

    @Transactional
    public SupplierResponse addContact(
            String supplierCode,
            AddSupplierContactRequest request
    ) {
        SupplierEntity supplier = repository.findByCode(supplierCode)
                .orElseThrow(() -> new BusinessException(
                        "SUPPLIER_NOT_FOUND",
                        "Supplier not found for code: " + supplierCode
                ));

        if (request.email() != null && !request.email().isBlank()) {
            boolean contactAlreadyExists = contactRepository.existsBySupplierIdAndEmail(
                    supplier.getId(),
                    request.email()
            );

            if (contactAlreadyExists) {
                throw new BusinessException(
                        "SUPPLIER_CONTACT_ALREADY_EXISTS",
                        "Supplier contact already exists with email: " + request.email()
                );
            }
        }

        SupplierContactEntity contact = new SupplierContactEntity();

        contact.setId(UUID.randomUUID());
        contact.setSupplier(supplier);
        contact.setName(request.name());
        contact.setEmail(request.email());
        contact.setPhone(request.phone());
        contact.setPosition(request.position());
        contact.setPrimaryContact(Boolean.TRUE.equals(request.primaryContact()));
        contact.setActive(true);
        contact.setCreatedAt(LocalDateTime.now());

        contactRepository.save(contact);

        return toResponse(supplier);
    }

    @Transactional
    public SupplierResponse updateContact(
            String supplierCode,
            UUID contactId,
            UpdateSupplierContactRequest request
    ) {
        SupplierEntity supplier = repository.findByCode(supplierCode)
                .orElseThrow(() -> new BusinessException(
                        "SUPPLIER_NOT_FOUND",
                        "Supplier not found for code: " + supplierCode
                ));

        SupplierContactEntity contact = contactRepository
                .findByIdAndSupplierId(contactId, supplier.getId())
                .orElseThrow(() -> new BusinessException(
                        "SUPPLIER_CONTACT_NOT_FOUND",
                        "Supplier contact not found: " + contactId
                ));

        contact.setName(request.name());
        contact.setEmail(request.email());
        contact.setPhone(request.phone());
        contact.setPosition(request.position());
        contact.setPrimaryContact(request.primaryContact());
        contact.setActive(request.active());

        contactRepository.save(contact);

        return toResponse(supplier);
    }

    @Transactional
    public SupplierResponse deactivateContact(
            String supplierCode,
            UUID contactId
    ) {
        SupplierEntity supplier = repository.findByCode(supplierCode)
                .orElseThrow(() -> new BusinessException(
                        "SUPPLIER_NOT_FOUND",
                        "Supplier not found for code: " + supplierCode
                ));

        SupplierContactEntity contact = contactRepository
                .findByIdAndSupplierId(contactId, supplier.getId())
                .orElseThrow(() -> new BusinessException(
                        "SUPPLIER_CONTACT_NOT_FOUND",
                        "Supplier contact not found: " + contactId
                ));

        contact.setActive(false);

        contactRepository.save(contact);

        return toResponse(supplier);
    }
}