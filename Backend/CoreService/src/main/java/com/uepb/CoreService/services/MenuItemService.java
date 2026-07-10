package com.uepb.CoreService.services;

import com.uepb.CoreService.domain.Cafeteria;
import com.uepb.CoreService.domain.MenuItem;
import com.uepb.CoreService.dto.request.MenuItemRequest;
import com.uepb.CoreService.dto.response.MenuItemResponse;
import com.uepb.CoreService.exceptions.CafeteriaIsNotActive;
import com.uepb.CoreService.exceptions.MenuItemAlreadyExists;
import com.uepb.CoreService.exceptions.MenuItemNotFound;
import com.uepb.CoreService.exceptions.NoMenuItemsYet;
import com.uepb.CoreService.repository.MenuItemRepository;
import com.uepb.CoreService.utils.StorageImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class MenuItemService {

    @Autowired
    MenuItemRepository menuItemsRepository;

    @Autowired
    private StorageImageService imageService;

    @Transactional
    public MenuItemResponse createMenuItem(Cafeteria cafeteria, MenuItemRequest newMenuItem){
        if(!cafeteria.isActive()){
            throw new CafeteriaIsNotActive(cafeteria.getName());
        }

        // impede que items com o mesmo nome sejam criados na mesma lanchonete
        List<MenuItem> items = menuItemsRepository.findByCafeteriaId(cafeteria.getId());
        for(MenuItem item: items){
            if(item.getName().equals(newMenuItem.name())){
                throw new MenuItemAlreadyExists(newMenuItem.name());
            }
        }

        MenuItem menuItem = MenuItem.builder()
                .cafeteria(cafeteria)
                .name(newMenuItem.name())
                .description(newMenuItem.description())
                .price(newMenuItem.price())
                .category(newMenuItem.category())
                .availabilityMode(newMenuItem.availabilityMode())
                .stock(newMenuItem.stock())
                .build();

        menuItemsRepository.save(menuItem);
        return toResponse(menuItem);
    }

    public String saveImage(Cafeteria cafeteria, String name, MultipartFile file) {
        MenuItem item = menuItemsRepository.findByCafeteriaIdAndName(cafeteria.getId(), name).orElseThrow(
                () -> new MenuItemNotFound(name)
        );
        String subfolder = "items-" + cafeteria.getName() + "/";
        String imagePath = imageService.saveImage(file, subfolder, item.getId(), item.getName());
        item.setImageUrl(imagePath);
        menuItemsRepository.save(item);
        return imagePath;
    }

    public List<MenuItemResponse> getMenuItemsForCafeteria(String cafeteriaId){
        List<MenuItem> menu = menuItemsRepository.findByCafeteriaId(cafeteriaId);
        if(menu.isEmpty()){
            throw new NoMenuItemsYet();
        }

        List<MenuItemResponse> menuResponse = new ArrayList<>();
        for(MenuItem menuItem : menu){
            if(menuItem.isAvailability()){
                menuResponse.add(toResponse(menuItem));
            }
        }
        return menuResponse;
    }

    public List<MenuItemResponse> getAllMyItems(Cafeteria cafeteria){
        List<MenuItem> menu = menuItemsRepository.findByCafeteriaId(cafeteria.getId());
        if(menu.isEmpty()){
            throw new NoMenuItemsYet();
        }
        List<MenuItemResponse> menuResponse = new ArrayList<>();
        for(MenuItem menuItem : menu){
            menuResponse.add(toResponse(menuItem));
        }
        return menuResponse;
    }

    public MenuItemResponse addStock(Cafeteria cafeteria, String itemName, Integer quantity){
        if(quantity <= 0){
            throw new IllegalArgumentException("Impossível adicionar um valor menor que 0: " + quantity);
        }
        MenuItem item = menuItemsRepository.findByCafeteriaIdAndName(cafeteria.getId(), itemName).orElseThrow(
                () -> new MenuItemNotFound(itemName)
        );
        item.setStock(item.getStock() + quantity);
        return toResponse(menuItemsRepository.save(item));
    }

    public MenuItemResponse removeStock(Cafeteria cafeteria, String itemName, Integer quantity){
        if(quantity <= 0){
            throw new IllegalArgumentException("Impossível remover um valor menor que 0: " + quantity);
        }
        MenuItem item = menuItemsRepository.findByCafeteriaIdAndName(cafeteria.getId(), itemName).orElseThrow(
                () -> new MenuItemNotFound(itemName)
        );
        if(item.getStock() < quantity){
            throw new IllegalArgumentException("Estoque indisponível. Estoque atual: " + item.getStock());
        }
        item.setStock(item.getStock() - quantity);
        return toResponse(menuItemsRepository.save(item));
    }

    public MenuItemResponse changeAvailability(Cafeteria cafeteria, String itemName, Boolean currentAvailability){
        MenuItem item = menuItemsRepository.findByCafeteriaIdAndName(cafeteria.getId(), itemName).orElseThrow(
                () -> new MenuItemNotFound(itemName)
        );
        item.setAvailability(currentAvailability);
        return toResponse(menuItemsRepository.save(item));
    }

    public MenuItemResponse updateItem(Cafeteria cafeteria, String itemName, MenuItemRequest newMenuItem){
        MenuItem item = menuItemsRepository.findByCafeteriaIdAndName(cafeteria.getId(), itemName).orElseThrow(
                () -> new MenuItemNotFound(itemName)
        );
        if(newMenuItem.name() != null){
            item.setName(newMenuItem.name());
        }
        if(newMenuItem.description() != null){
            item.setDescription(newMenuItem.description());
        }
        if(newMenuItem.price() != null){
            item.setPrice(newMenuItem.price());
        }
        if(newMenuItem.category() != null){
            item.setCategory(newMenuItem.category());
        }
        if(newMenuItem.availabilityMode() != null){
            item.setAvailabilityMode(newMenuItem.availabilityMode());
        }
        if(newMenuItem.stock() != null){
            item.setStock(newMenuItem.stock());
        }
        return toResponse(menuItemsRepository.save(item));
    }

    public void deleteItem(Cafeteria cafeteria, String itemName){
        MenuItem item = menuItemsRepository.findByCafeteriaIdAndName(cafeteria.getId(), itemName).orElseThrow(
                () -> new MenuItemNotFound(itemName)
        );
        menuItemsRepository.delete(item);
    }

    private MenuItemResponse toResponse(MenuItem menuItem){
        return new MenuItemResponse(
                menuItem.getCafeteria().getName(),
                menuItem.getName(),
                menuItem.getDescription(),
                menuItem.getPrice(),
                menuItem.getCategory(),
                menuItem.getStock()
        );
    }
}
