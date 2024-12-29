package com.github.akarazhev.activej.examples.decoder;

import java.util.List;

interface ContactDAO {
    List<Contact> list();

    void add(Contact user);
}
