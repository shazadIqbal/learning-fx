
package com.mls.learningfx.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {

    private int id;
    private int typeId;

    private String type;
    private String name;
    private String contact;
    private String companyName;
}
