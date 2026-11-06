
package com.mls.learningfx.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class Customer {

    private Integer id;
    private String name;
    private String contact;

    @Override
    public String toString() {
        return "Customer: " +
                "ID = " + id +
                ", Name = '" + name + '\'' +
                ", Contact = '" + contact + '\'';
    }
}
