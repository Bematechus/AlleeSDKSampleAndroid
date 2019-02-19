# AlleeSDK
AlleeSDK connects via TCP-IP communication point of sales software to kitchenGo allee software.  
This SDK works for Android platform only.

## Installation
### Gradle
    implementation 'com.bematech:alleesdk:1.0.2'
  
### Maven
    <dependency>
      <groupId>com.bematech</groupId>
      <artifactId>alleesdk</artifactId>
      <version>1.0.2</version>
      <type>pom</type>
    </dependency>
  

## Usage
### Basic
To start use our AlleeSDK you need start it in your AppDelegate. I will need a STORE_KEY to do that:

    AlleeSDK.shared.start(CONTEXT, STORE_KEY, PORT)
    

Allee orders are made with one `AlleeOrder` with any `AlleeItem` with any `AlleeCondiment`.  
So first we need create the condiments:

    val condiment = AlleeCondiment()
    condiment.id = UUID.randomUUID().toString()
    condiment.name = "Tomatoes"
        
Then we can to create our items, inserting our condiments:

    val item = AlleeItem()
    item.id = UUID.randomUUID().toString()
    item.name = "Veggie Burger"
    item.kdsStation = "1" // Target KDS preparation station
    item.quantity = 3
    item.condiments = arrayListOf(condiment)
    
    
We can also add a item recipe using `AlleeItemRecipe`: 

    val recipe = AlleeItemRecipe()
    recipe.image = "https://bit.ly/2I9dkxH"
    recipe.ingredients = arrayListOf("1/2 medium yellow onion, chopped", "3/4 c. panko bread crumbs", "1 tomato, sliced")
    recipe.steps = arrayListOf("In a food processor, pulse black beans, onion, and garlic until finely chopped.",
        "Transfer to a large bowl and combine with egg, 2 tablespoons mayo, and panko.",
        "In a large skillet over medium heat, heat oil.",
        "Add patties and cook until golden and warmed through, about 5 minutes per side.")
                    
    item.itemRecipe = recipe
        
        
And to create our order, using the created items:

    val order = AlleeOrder()
    order.id = "1"
    order.items = arrayListOf(item)
        

We can also add a customer to order, using `AlleeCustomer`. **All customer data will be encrypted**:

    val customer = AlleeCustomer()
    customer.id = UUID.randomUUID().toString()
    customer.name = "NAME"
    customer.phone = "PHONE"

    order.customer = customer
    
        
Now we need send this order to KDS, to do that we will use the `AlleeSDK.shared`:

    AlleeSDK.shared.sendOrder(order, Callback { error ->
        if (error != null) {
            print(error)

        } else {
            print("Order sent")
        }
    })
        

### Orders preparation status
The AlleeSDK provides an observer for your POS to receive the orders status.  It is triggered automatically when some order status is changed, but you can also request manually if you need.

Set the listener (**OrdersBumpStatusListener**) before the start:

    AlleeSDK.shared.ordersBumpStatusListener = this

And implement the method:

    override fun updatedOrdersBumpStatus(p0: MutableList<AlleeOrderBumpStatus>?) {
    
    }
    
If you need you can request the orders status manually (Optional):

    AlleeSDK.shared.requestOrdersStatus(Callback { error ->
        error.let {
            print(error)
        }
    })
    
**Note**: Only the new orders status is received.  
  
### Full Models
Still, if you need to provide more information in your order, please check all our orders data:

#### AlleeOrder

    String id; // Order ID
    int posTerminal = 0; // POS Terminal ID
    String guestTable; // Table name
    String serverName; // Server Name
    String destination; // Destination (per exemple: Drive Thru, Diner, Delivery)
    String userInfo; // Another information about customer
    List<String> orderMessages; // Custom messages
    int transType = AlleeTransType.INSERT; // Type of transaction (INSERT, DELETE, UPDATE)
    AlleeOrder.OrderType orderType = OrderType.REGULAR; // Order priority (REGULAR, FIRE, RUSH)
    List<AlleeItem> items; // Order items
    AlleeCustomer customer; // Order customer
    
    
#### AlleeItem

    String id; // Item ID
    String name; // Item Name
    String buildCard; // A text or a link with steps to prepare the item
    String trainingVideo; // A video link with steps to prepare the item
    List<String> preModifier; // Item custom messages
    Double preparationTime; // How long time to prepare this item (in minutes)
    int quantity; // Item quantity
    String kdsStation; // Target KDS preparation station
    int transType = AlleeTransType.INSERT; // Type of transaction (INSERT, DELETE, UPDATE)
    List<AlleeCondiment> condiments; // Item condiments
    AlleeSummary summary; // Summary of this item
    AlleeItemRecipe itemRecipe; // Recipe of this item
    ItemType itemType = ItemType.REGULAR; // Item priority (REGULAR, FIRE)
    
    
#### AlleeCondiment

    String id; // Condiment ID
    String name; // Condiment Name
    List<String> preModifier; // Condiment custom messages
    Double preparationTime; // Type of transaction (INSERT, DELETE, UPDATE)
    int transType = AlleeTransType.INSERT; // How long time to prepare this condiment (in minutes)
    
    
#### AlleeCustomer

    String id; // Customer ID
    String name; // Customer name
    String phone; // Customer phone
    String phone2; // Customer phone2
    String address; // Customer address
    String address2; // Customer address2
    String city; // Customer City
    String stage; // Customer state
    String zip; // Customer zip code
    String country; // Customer country
    String email; // Customer E-mail
    String webmail; // Customer webmail 
    int transType = AlleeTransType.INSERT; // Type of transaction (INSERT, DELETE, UPDATE)
    
        
#### AlleeSummary

    String ingredientName; // Ingredient name
    int ingredientQuantity; // Quantity of this ingredient
    

#### AlleeItemRecipe

    String image; // Image URL
    List<String> ingredients; // List of ingredients
    List<String> steps; // List of steps
    int transType = AlleeTransType.INSERT; // Type of transaction (INSERT, DELETE, UPDATE)

