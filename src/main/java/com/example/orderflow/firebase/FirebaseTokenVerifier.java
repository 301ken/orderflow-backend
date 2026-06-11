package com.example.orderflow.firebase;

public interface FirebaseTokenVerifier {

    FirebaseUser verify(String idToken);
}
