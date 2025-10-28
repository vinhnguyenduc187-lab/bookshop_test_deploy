package vn.edu.iuh.fit.bookshop_be.services;

import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.bookshop_be.models.Address;
import vn.edu.iuh.fit.bookshop_be.models.Customer;
import vn.edu.iuh.fit.bookshop_be.repositories.AddressRepository;

import java.util.List;

@Service
public class AddressService {
    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public Address save(Address address) {
        return addressRepository.save(address);
    }

    public Address findById(Integer id) {
        return addressRepository.findById(id).orElse(null);
    }

    public boolean deleteById(Integer id) {
        if (addressRepository.existsById(id)) {
            addressRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<Address> findByCustomer(Customer customer) {
        return addressRepository.findByCustomer(customer);
    }

    public Address findByIdAndUser(Integer id, Customer customer) {
        return addressRepository.findByIdAndCustomer(id, customer);
    }


}
