package com.uepb.CoreService.controllers;

import com.uepb.CoreService.domain.Cafeteria;
import com.uepb.CoreService.domain.MenuItem;
import com.uepb.CoreService.dto.request.ChangeStockRequest;
import com.uepb.CoreService.dto.request.MenuItemRequest;
import com.uepb.CoreService.dto.response.MenuItemResponse;
import com.uepb.CoreService.services.MenuItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/menuItem")
public class MenuItemController {

    @Autowired
    private MenuItemService menuItemService;

    @PostMapping("/create")
    public ResponseEntity<MenuItemResponse> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid MenuItemRequest newMenuItem
    ){

        MenuItemResponse item = menuItemService.createMenuItem((Cafeteria) userDetails, newMenuItem);
        return ResponseEntity.ok(item);
    }

    @PostMapping(value = "/item/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadImagem(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("name") String name,
            @RequestParam("image") MultipartFile image
    ) {

        String urlImagem = menuItemService.saveImage((Cafeteria) userDetails, name, image);
        return ResponseEntity.ok(urlImagem);
    }

    @GetMapping(value = "/me/items/all")
    public ResponseEntity<List<MenuItemResponse>> getAllItems(@AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.ok(menuItemService.getAllMyItems((Cafeteria) userDetails));
    }

    @PostMapping(value = "/addStock")
    public ResponseEntity<MenuItemResponse> addStock(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid ChangeStockRequest infos
    ){
        MenuItemResponse item = menuItemService.addStock((Cafeteria) userDetails, infos.itemName(), infos.quantity());
        return ResponseEntity.ok(item);
    }

    @PostMapping(value = "/removeStock")
    public ResponseEntity<MenuItemResponse> removeStock(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid ChangeStockRequest infos
    ){
        MenuItemResponse item = menuItemService.removeStock((Cafeteria) userDetails, infos.itemName(), infos.quantity());
        return ResponseEntity.ok(item);
    }

    @PatchMapping(value = "/{itemName}/lockItem")
    public ResponseEntity<MenuItemResponse> blockMenuItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String itemName
    ){
        MenuItemResponse item = menuItemService.changeAvailability((Cafeteria) userDetails,itemName, false);
        return ResponseEntity.ok(item);
    }

    @PatchMapping(value = "/{itemName}/unlockItem")
    public ResponseEntity<MenuItemResponse> unblockMenuItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String itemName
    ){
        MenuItemResponse item = menuItemService.changeAvailability((Cafeteria) userDetails,itemName, true);
        return ResponseEntity.ok(item);
    }

    @PatchMapping(value = "/{itemName}/update")
    public ResponseEntity<MenuItemResponse> updateItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String itemName,
            @RequestBody @Valid MenuItemRequest updatedItem
    ){
        MenuItemResponse item = menuItemService.updateItem((Cafeteria) userDetails, itemName, updatedItem);
        return ResponseEntity.ok(item);
    }

    @DeleteMapping(value = "/{itemName}/delete")
    public ResponseEntity<MenuItemResponse> deleteItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String itemName
    ){
        menuItemService.deleteItem((Cafeteria) userDetails,itemName);
        return ResponseEntity.ok().build();
    }
}
