package ma.fstm.ilisi.projects.webmvc.service;
import ma.fstm.ilisi.projects.webmvc.bo.Address;
import ma.fstm.ilisi.projects.webmvc.bo.User;
import ma.fstm.ilisi.projects.webmvc.dto.AddressDTO;
import ma.fstm.ilisi.projects.webmvc.repository.AddressRepository;
import ma.fstm.ilisi.projects.webmvc.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
@Transactional
public class AddressService {
    @Autowired
    private AddressRepository addressRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // ==================== CONVERSION METHODS ====================
    
    public AddressDTO toAddressDTO(Address address) {
        if (address == null) return null;
        
        AddressDTO dto = new AddressDTO();
        dto.setId(address.getId());
        dto.setCity(address.getCity());
        dto.setPostalCode(address.getPostalCode());
        dto.setCountry(address.getCountry());
        
        // Informations sur l'utilisateur associé
        if (address.getUser() != null) {
            dto.setUserId(address.getUser().getId());
            dto.setUserFullName(address.getUser().getFullName());
        }
        
        return dto;
    }
    public Address toAddress(AddressDTO dto) {
        if (dto == null) return null;
        
        Address address = new Address();
        address.setId(dto.getId());
        address.setCity(dto.getCity());
        address.setPostalCode(dto.getPostalCode());
        address.setCountry(dto.getCountry());
        
        return address;
    }
    
    // ==================== CRUD METHODS ====================
    
    public AddressDTO createAddress(AddressDTO addressDTO) {
        Address address = toAddress(addressDTO);
        
        // Associer à un utilisateur si userId est fourni
        if (addressDTO.getUserId() != null) {
            Optional<User> userOpt = userRepository.findById(addressDTO.getUserId());
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                address.setUser(user);
                user.setAddress(address);
            } else {
                throw new RuntimeException("Utilisateur non trouvé avec l'ID: " + addressDTO.getUserId());
            }
        }
        
        Address savedAddress = addressRepository.save(address);
        return toAddressDTO(savedAddress);
    }
    public List<AddressDTO> getAllAddresses() {
        return addressRepository.findAll().stream()
                .map(this::toAddressDTO)
                .collect(Collectors.toList());
    }
    public AddressDTO getAddressById(int id) {
        return addressRepository.findById(id)
                .map(this::toAddressDTO)
                .orElse(null);
    }
    
    public AddressDTO getAddressByUserId(int userId) {
        return addressRepository.findByUserId(userId)
                .map(this::toAddressDTO)
                .orElse(null);
    }
    public AddressDTO updateAddress(AddressDTO addressDTO) {
        Optional<Address> addressOpt = addressRepository.findById(addressDTO.getId());
        
        if (addressOpt.isPresent()) {
            Address address = addressOpt.get();
            
            address.setCity(addressDTO.getCity());
            address.setPostalCode(addressDTO.getPostalCode());
            address.setCountry(addressDTO.getCountry());
            
            // Gestion du changement d'utilisateur
            if (addressDTO.getUserId() != null) {
                if (address.getUser() == null || address.getUser().getId() != addressDTO.getUserId()) {
                    // Retirer l'ancienne association
                    if (address.getUser() != null) {
                        address.getUser().setAddress(null);
                    }
                    
                    // Nouvelle association
                    Optional<User> newUserOpt = userRepository.findById(addressDTO.getUserId());
                    if (newUserOpt.isPresent()) {
                        User newUser = newUserOpt.get();
                        address.setUser(newUser);
                        newUser.setAddress(address);
                    }
                }
            } else {
                // Si userId est null, on dissocie l'adresse
                if (address.getUser() != null) {
                    address.getUser().setAddress(null);
                    address.setUser(null);
                }
            }
            
            Address updatedAddress = addressRepository.save(address);
            return toAddressDTO(updatedAddress);
        }
        
        return null;
    }
    public boolean deleteAddress(int id) {
        Optional<Address> addressOpt = addressRepository.findById(id);
        
        if (addressOpt.isPresent()) {
            Address address = addressOpt.get();
            
            if (address.getUser() != null) {
                address.getUser().setAddress(null);
            }
            
            addressRepository.delete(address);
            return true;
        }
        return false;
    }
    
    // ==================== SEARCH METHODS ====================
    
    public List<AddressDTO> getAddressesByCity(String city) {
        return addressRepository.findByCity(city)
                .stream()
                .map(this::toAddressDTO)
                .collect(Collectors.toList());
    }
    
    public List<AddressDTO> getAddressesByCountry(String country) {
        return addressRepository.findByCountry(country)
                .stream()
                .map(this::toAddressDTO)
                .collect(Collectors.toList());
    }
    
    public List<AddressDTO> getAddressesByCityAndCountry(String city, String country) {
        return addressRepository.findByCityAndCountry(city, country)
                .stream()
                .map(this::toAddressDTO)
                .collect(Collectors.toList());
    }
    
    public List<AddressDTO> getAddressesByPostalCode(String postalCode) {
        return addressRepository.findByPostalCode(postalCode)
                .stream()
                .map(this::toAddressDTO)
                .collect(Collectors.toList());
    }
    
    // Ces méthodes utilisent maintenant les nouvelles méthodes du repository
    public List<AddressDTO> searchAddresses(String city, String country, String postalCode) {
        return addressRepository.searchAddresses(city, country, postalCode)
                .stream()
                .map(this::toAddressDTO)
                .collect(Collectors.toList());
    }
    
    public List<AddressDTO> getUnassignedAddresses() {
        return addressRepository.findUnassignedAddresses()
                .stream()
                .map(this::toAddressDTO)
                .collect(Collectors.toList());
    }
    
    // ==================== BUSINESS METHODS ====================
    
    @Transactional
    public AddressDTO assignAddressToUser(int addressId, int userId) {
        Optional<Address> addressOpt = addressRepository.findById(addressId);
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (addressOpt.isPresent() && userOpt.isPresent()) {
            Address address = addressOpt.get();
            User user = userOpt.get();
            
            // Vérifier si l'utilisateur a déjà une adresse
            if (user.getAddress() != null && user.getAddress().getId() != addressId) {
                user.getAddress().setUser(null);
            }
            
            // Vérifier si l'adresse est déjà assignée
            if (address.getUser() != null && address.getUser().getId() != userId) {
                address.getUser().setAddress(null);
            }
            
            address.setUser(user);
            user.setAddress(address);
            
            addressRepository.save(address);
            userRepository.save(user);
            
            return toAddressDTO(address);
        }
        
        return null;
    }
    
    @Transactional
    public boolean unassignAddressFromUser(int addressId) {
        Optional<Address> addressOpt = addressRepository.findById(addressId);
        
        if (addressOpt.isPresent()) {
            Address address = addressOpt.get();
            
            if (address.getUser() != null) {
                User user = address.getUser();
                user.setAddress(null);
                address.setUser(null);
                
                userRepository.save(user);
                addressRepository.save(address);
            }
            
            return true;
        }
        
        return false;
    }
    
    // ==================== STATISTICS METHODS ====================
    
    public long getTotalAddresses() {
        return addressRepository.count();
    }
    
    public List<Object[]> getAddressesCountByCountry() {
        return addressRepository.countAddressesByCountry();
    }
    
    public List<Object[]> getAddressesCountByCity() {
        return addressRepository.countAddressesByCity();
    }
    
    public List<Object[]> getAddressesCountByPostalCode() {
        return addressRepository.countAddressesByPostalCode();
    }
    
    public boolean isAddressAssigned(int addressId) {
        return addressRepository.isAddressAssignedToUser(addressId);
    }
    
    // ==================== VALIDATION METHODS ====================
    
    public boolean addressExists(int id) {
        return addressRepository.existsById(id);
    }
    
    public boolean hasUserAddress(int userId) {
        return addressRepository.existsByUserId(userId);
    }
    
    public AddressDTO getOrCreateAddressForUser(int userId, AddressDTO addressDTO) {
        Optional<Address> existingAddress = addressRepository.findByUserId(userId);
        
        if (existingAddress.isPresent()) {
            Address address = existingAddress.get();
            address.setCity(addressDTO.getCity());
            address.setPostalCode(addressDTO.getPostalCode());
            address.setCountry(addressDTO.getCountry());
            return toAddressDTO(addressRepository.save(address));
        } else {
            addressDTO.setUserId(userId);
            return createAddress(addressDTO);
        }
    }
}