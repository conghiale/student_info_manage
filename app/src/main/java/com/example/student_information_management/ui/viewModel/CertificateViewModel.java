package com.example.student_information_management.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.student_information_management.data.model.Certificate;
import com.example.student_information_management.data.repository.CertificateRepository;

import java.util.List;

public class CertificateViewModel extends ViewModel {
    private MutableLiveData<List<Certificate>> certificates = new MutableLiveData<>();
    private CertificateRepository certificateRepository = new CertificateRepository();

    public LiveData<List<Certificate>> getCertificates(String idStudent) {
        loadCertificates(idStudent);
        return certificates;
    }

    public void loadCertificates(String idStudent) {
        certificateRepository.getCertificates(idStudent, certificates);
    }
}
