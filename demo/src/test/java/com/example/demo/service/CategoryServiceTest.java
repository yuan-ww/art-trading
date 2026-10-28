package com.example.demo.service;

import com.example.demo.model.Category;
import com.example.demo.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {
    @Mock CategoryRepository repository;
    @InjectMocks CategoryService service;

    @Test void categoryCacheIsCopiedAndInvalidatedAfterCreate() {
        Category first = new Category("国画", "描述");
        when(repository.findAll()).thenReturn(List.of(first), List.of(first, new Category("油画", "")));
        List<Category> result = service.findAll();
        result.get(0).setName("篡改");
        assertEquals("国画", service.findAll().get(0).getName());
        when(repository.existsByName("油画")).thenReturn(false);
        service.createCategory(new Category("油画", ""));
        assertEquals(2, service.findAll().size());
        verify(repository, times(2)).findAll();
    }
}
