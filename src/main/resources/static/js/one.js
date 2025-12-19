
// Shopping List JavaScript
document.addEventListener('DOMContentLoaded', function() {

    // ========== Toggle Item Completion (Optional - for immediate visual feedback) ==========
    const toggleForms = document.querySelectorAll('form[action*="/toggle/"]');

    toggleForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            // Optional: Add visual feedback before form submits
            const item = form.closest('.shopping-item');
            if (item) {
                item.style.opacity = '0.5';
                item.style.transition = 'opacity 0.3s ease';
            }
        });
    });

    // ========== Confirm Delete Actions ==========
    const deleteForms = document.querySelectorAll('form[action*="/delete/"]');

    deleteForms.forEach(form => {
        form.addEventListener('submit', function(e) {
            if (!confirm('Are you sure you want to delete this item?')) {
                e.preventDefault();
            }
        });
    });

    // ========== Bulk Actions Confirmation ==========
    const markAllForm = document.querySelector('form[action*="/mark-all-complete"]');
    if (markAllForm) {
        markAllForm.addEventListener('submit', function(e) {
            if (!confirm('Mark all items as complete?')) {
                e.preventDefault();
            }
        });
    }

    const removeCompletedForm = document.querySelector('form[action*="/remove-completed"]');
    if (removeCompletedForm) {
        removeCompletedForm.addEventListener('submit', function(e) {
            if (!confirm('Remove all completed items?')) {
                e.preventDefault();
            }
        });
    }

    const clearAllForm = document.querySelector('form[action*="/clear-all"]');
    if (clearAllForm) {
        clearAllForm.addEventListener('submit', function(e) {
            if (!confirm('Clear entire shopping list? This cannot be undone.')) {
                e.preventDefault();
            }
        });
    }

    // ========== Sidebar Toggle ==========
    const sidebarToggle = document.querySelector('.sidebar-toggle');
    const dashboardContainer = document.querySelector('.dashboard-container');

    if (sidebarToggle && dashboardContainer) {
        sidebarToggle.addEventListener('click', function() {
            dashboardContainer.classList.toggle('sidebar-collapsed');
        });
    }

    // ========== Add Item Form Enhancement ==========
    const addItemForm = document.querySelector('form[action*="/add"]');
    if (addItemForm) {
        const nameInput = addItemForm.querySelector('input[name="name"]');


        if (nameInput) {
            nameInput.focus();
        }


    }

    // ========== Category Section Collapse (Optional Enhancement) ==========
    const categoryHeaders = document.querySelectorAll('.category-header');

    categoryHeaders.forEach(header => {
        header.style.cursor = 'pointer';

        header.addEventListener('click', function() {
            const itemsContainer = this.nextElementSibling;
            if (itemsContainer && itemsContainer.classList.contains('list-group')) {
                itemsContainer.style.display =
                    itemsContainer.style.display === 'none' ? 'block' : 'none';

                const icon = this.querySelector('i');
                if (icon) {
                    icon.classList.toggle('bi-tag-fill');
                    icon.classList.toggle('bi-tag');
                }
            }
        });
    });

    // ========== Smooth Scroll for Long Lists ==========
    const shoppingItems = document.querySelectorAll('.shopping-item');
    if (shoppingItems.length > 10) {

        const backToTopBtn = document.createElement('button');
        backToTopBtn.innerHTML = '<i class="bi bi-arrow-up-circle-fill"></i>';
        backToTopBtn.className = 'btn btn-primary position-fixed bottom-0 end-0 m-4';
        backToTopBtn.style.display = 'none';
        backToTopBtn.style.zIndex = '1000';

        document.body.appendChild(backToTopBtn);

        window.addEventListener('scroll', function() {
            if (window.scrollY > 300) {
                backToTopBtn.style.display = 'block';
            } else {
                backToTopBtn.style.display = 'none';
            }
        });

        backToTopBtn.addEventListener('click', function() {
            window.scrollTo({ top: 0, behavior: 'smooth' });
        });
    }

    // ========== Recipe Dropdown Enhancement ==========
    const recipeSelect = document.querySelector('select[name="recipeId"]');
    if (recipeSelect) {
        recipeSelect.addEventListener('change', function() {
            if (this.value) {

                console.log('Selected recipe:', this.value);
            }
        });
    }

    // ========== Animation for Newly Added Items (if you want) ==========

    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('added') === 'true') {
        const lastItem = document.querySelector('.shopping-item:first-child');
        if (lastItem) {
            lastItem.style.animation = 'fadeInSlide 0.5s ease';
        }
    }
});


// ========== Recipe Form - Dynamic Ingredients ==========
const ingredientsContainer = document.getElementById('ingredientsContainer');
const ingredientTemplate = document.getElementById('ingredient-template');

if (ingredientsContainer && ingredientTemplate) {
    let ingredientIndex = ingredientsContainer.querySelectorAll('.ingredient-row').length;


       const addButton = document.createElement('button');
    addButton.type = 'button';
    addButton.className = 'btn btn-outline-primary mb-3';


    const addMoreText = document.getElementById('addMoreIngredientText');
    const buttonText = addMoreText ? addMoreText.textContent : 'Add More Ingredient';
    addButton.innerHTML = `<i class="bi bi-plus-circle me-2"></i>${buttonText}`;


    const helpText = ingredientsContainer.nextElementSibling;
    if (helpText && helpText.classList.contains('form-text')) {
        helpText.insertAdjacentElement('afterend', addButton);
    } else {
        ingredientsContainer.insertAdjacentElement('afterend', addButton);
    }


    addButton.addEventListener('click', function() {

        const newRow = ingredientTemplate.content.cloneNode(true);
        const rowDiv = newRow.querySelector('.ingredient-row');


        const nameInput = newRow.querySelector('input[type="text"]');
        const quantityInput = newRow.querySelectorAll('input[type="text"]')[1];
        const unitSelect = newRow.querySelector('select');
        const notesInput = newRow.querySelectorAll('input[type="text"]')[2];

        nameInput.name = `recipeIngredients[${ingredientIndex}].ingredientName`;
        nameInput.id = `ingredientName${ingredientIndex}`;

        quantityInput.name = `recipeIngredients[${ingredientIndex}].quantity`;
        quantityInput.id = `quantity${ingredientIndex}`;

        unitSelect.name = `recipeIngredients[${ingredientIndex}].unit`;
        unitSelect.id = `unit${ingredientIndex}`;

        notesInput.name = `recipeIngredients[${ingredientIndex}].notes`;
        notesInput.id = `notes${ingredientIndex}`;


        const labels = newRow.querySelectorAll('label');
        if (labels[0]) labels[0].setAttribute('for', `ingredientName${ingredientIndex}`);
        if (labels[1]) labels[1].setAttribute('for', `quantity${ingredientIndex}`);
        if (labels[2]) labels[2].setAttribute('for', `unit${ingredientIndex}`);
        if (labels[3]) labels[3].setAttribute('for', `notes${ingredientIndex}`);


        const removeBtn = newRow.querySelector('.remove-ingredient');
        removeBtn.addEventListener('click', function() {
            rowDiv.remove();
        });

        ingredientsContainer.appendChild(newRow);
        ingredientIndex++;
    });


    document.querySelectorAll('.remove-ingredient').forEach(btn => {
        btn.addEventListener('click', function() {
            this.closest('.ingredient-row').remove();
        });
    });
}


// ========== Recipe Detail - Select All Ingredients ==========
function selectAllIngredients() {
    document.querySelectorAll('input[name="selectedIngredients"]').forEach(cb => cb.checked = true);
}

window.selectAllIngredients = selectAllIngredients;

function shareRecipe() {
    const recipeUrl = window.location.href;
    const recipeTitle = document.querySelector('h1').textContent;


    navigator.clipboard.writeText(recipeUrl).then(function() {

        showToast('Link copied! Share: ' + recipeTitle);
    }).catch(function(err) {

        const tempInput = document.createElement('input');
        tempInput.value = recipeUrl;
        document.body.appendChild(tempInput);
        tempInput.select();
        document.execCommand('copy');
        document.body.removeChild(tempInput);
        showToast('Link copied!');
    });
}

function showToast(message) {

    const toast = document.createElement('div');
    toast.className = 'position-fixed bottom-0 end-0 p-3';
    toast.style.zIndex = '9999';
    toast.innerHTML = `
        <div class="toast show" role="alert">
            <div class="toast-header bg-success text-white">
                <i class="bi bi-check-circle me-2"></i>
                <strong class="me-auto">Success</strong>
                <button type="button" class="btn-close btn-close-white" onclick="this.closest('.position-fixed').remove()"></button>
            </div>
            <div class="toast-body">
                ${message}
            </div>
        </div>
    `;

    document.body.appendChild(toast);


    setTimeout(() => {
        toast.remove();
    }, 3000);
}
// ========== CSS Animation (add to CSS if you use the animation above) ==========
// @keyframes fadeInSlide {
//     from {
//         opacity: 0;
//         transform: translateX(-20px);
//     }
//     to {
//         opacity: 1;
//         transform: translateX(0);
//     }
// }