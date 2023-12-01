package com.example.student_information_management.data.repository;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.student_information_management.data.model.Certificate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CertificateRepository {
    private FirebaseFirestore db;

    public CertificateRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public void addCertificate(Certificate certificate, OnSuccessListener<DocumentReference> onSuccessListener) {
        DocumentReference newCertRef = db.collection("certificates").document();
        certificate.setId(newCertRef.getId());
        newCertRef.set(certificate)
                .addOnSuccessListener(aVoid -> onSuccessListener.onSuccess(newCertRef))
                .addOnFailureListener(e -> Log.w("CertificateRepository", "Error adding document", e));
    }

    public void getCertificates(String idStudent, MutableLiveData<List<Certificate>> certificatesLiveData) {
        db.collection("certificates")
                .whereEqualTo("idStudent", idStudent)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Certificate> certificateList = new ArrayList<>();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Certificate certificate = documentSnapshot.toObject(Certificate.class);
                        certificateList.add(certificate);
                    }
                    certificatesLiveData.setValue(certificateList);
                });
    }

    public void deleteCertificate(Certificate certificate, OnCompleteListener<Void> onCompleteListener) {
        db.collection("certificates").document(certificate.getId())
                .delete()
                .addOnCompleteListener(onCompleteListener);
    }

    public void editCertificate(String certificateId, Map<String, Object> updatedCertificate, OnCompleteListener<Void> onCompleteListener) {
        db.collection("certificates").document(certificateId)
                .update(updatedCertificate)
                .addOnCompleteListener(onCompleteListener);
    }
}
