
package com.mls.learningfx.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupplierType {

    private int id;
    private String name;

    @Override
    public String toString() {
        return name;
    }
}
