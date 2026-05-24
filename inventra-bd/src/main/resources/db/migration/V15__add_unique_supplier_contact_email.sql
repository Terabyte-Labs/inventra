CREATE UNIQUE INDEX uq_supplier_contact_supplier_email
    ON catalog.supplier_contacts (supplier_id, email)
    WHERE email IS NOT NULL;