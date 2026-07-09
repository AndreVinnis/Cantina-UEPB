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
